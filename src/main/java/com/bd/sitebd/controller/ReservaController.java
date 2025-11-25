package com.bd.sitebd.controller;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservar")
    public String exibirFormulario(@RequestParam String numero, Model model) {
        Reserva reserva = new Reserva();
        reserva.setNumero(numero);
        model.addAttribute("reserva", reserva);
        model.addAttribute("reservaEfetuada", false);
        model.addAttribute("dataMinima", LocalDate.now().toString());
        return "reservar";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservar")
    public String realizarReserva(@ModelAttribute Reserva reserva, Model model, Authentication authentication) {
        try {
            reserva.setEmailRequisitor(authentication.getName()); 

            reserva.setGradeReserva(false);
            reservaService.salvar(reserva); 

            model.addAttribute("reservaEfetuada", true);
            Reserva novaReserva = new Reserva();
            novaReserva.setNumero(reserva.getNumero());
            model.addAttribute("reserva", novaReserva);

            return "reservar";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("reserva", reserva);
            model.addAttribute("reservaEfetuada", false);
            return "reservar";
        }
    }
     
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listagem")
    public String listarReservas(
            @RequestParam(name = "periodo", defaultValue = "15dias") String periodo,
            @RequestParam(name = "view", defaultValue = "all") String view,
            Model model,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        model.addAttribute("usuarioEmail", emailUsuario);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        List<Reserva> reservasParaFiltrar;

        if (isAdmin) {
            reservasParaFiltrar = reservaService.listarTodas();
        } else {
            reservasParaFiltrar = reservaService.listarPorUsuario(emailUsuario);
            view = "me";
        }

        List<Reserva> reservasPorVisao; 
        if (isAdmin) {
            if ("me".equals(view)) {
                reservasPorVisao = reservasParaFiltrar.stream()
                        .filter(r -> (r.getEmailRequisitor() != null && r.getEmailRequisitor().equals(emailUsuario))
                                || r.isGradeReserva()) 
                        .collect(Collectors.toList());
            } else if ("others".equals(view)) {
                reservasPorVisao = reservasParaFiltrar.stream()
                        .filter(r -> (r.getEmailRequisitor() != null && !r.getEmailRequisitor().equals(emailUsuario))
                                && !r.isGradeReserva()) 
                        .collect(Collectors.toList());
            } else {
                reservasPorVisao = reservasParaFiltrar;
            }
        } else {
            reservasPorVisao = reservasParaFiltrar;
        }

        LocalDate hoje = LocalDate.now();
        List<Reserva> reservasFinais;

        switch (periodo) {
            case "15dias":
                reservasFinais = reservasPorVisao.stream()
                        .filter(r -> r.getData() != null && !r.getData().isBefore(hoje)
                                && !r.getData().isAfter(hoje.plusDays(15)))
                        .collect(Collectors.toList());
                break;
            case "30dias":
                reservasFinais = reservasPorVisao.stream()
                        .filter(r -> r.getData() != null && !r.getData().isBefore(hoje)
                                && !r.getData().isAfter(hoje.plusDays(30)))
                        .collect(Collectors.toList());
                break;
            case "proximas":
                reservasFinais = reservasPorVisao.stream()
                        .filter(r -> r.getData() != null && !r.getData().isBefore(hoje))
                        .collect(Collectors.toList());
                break;
            case "anteriores":
                reservasFinais = reservasPorVisao.stream()
                        .filter(r -> r.getData() != null && r.getData().isBefore(hoje))
                        .collect(Collectors.toList());
                break;
            default:
                reservasFinais = reservasPorVisao.stream()
                        .filter(r -> r.getData() != null && !r.getData().isBefore(hoje)
                                && !r.getData().isAfter(hoje.plusDays(15)))
                        .collect(Collectors.toList());
                periodo = "15dias";
                break;
        }

        List<Reserva> reservasAuditorio = reservasFinais.stream()
                .filter(r -> "Auditorio".equalsIgnoreCase(r.getNumero()))
                .collect(Collectors.toList());

        List<Reserva> reservasSalas = reservasFinais.stream()
                .filter(r -> r.getNumero() != null && !"Auditorio".equalsIgnoreCase(r.getNumero()))
                .collect(Collectors.toList());

        
        model.addAttribute("reservasAuditorio", reservasAuditorio);
        model.addAttribute("reservasSalas", reservasSalas);
        model.addAttribute("activePage", "listagem");
        model.addAttribute("periodoSelecionado", periodo);
        model.addAttribute("viewSelecionada", view);

        return "listagem";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deletar/{id}")
    public String deletarReserva(@PathVariable Long id) {
        reservaService.deletar(id);
        return "redirect:/listagem";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservas/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        Optional<Reserva> optionalReserva = reservaService.buscarPorId(id);
        if (optionalReserva.isEmpty()) {
            return "redirect:/listagem";
        }

        Reserva reserva = optionalReserva.get();
        model.addAttribute("reserva", reserva);
        model.addAttribute("dataMinima", LocalDate.now().toString());
        return "atualizar";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservas/editar/{id}")
    public String atualizarReserva(@PathVariable Long id, @ModelAttribute Reserva reservaAtualizada, Model model) {
        Optional<Reserva> optionalReserva = reservaService.buscarPorId(id);
        if (optionalReserva.isEmpty()) {
            return "redirect:/listagem";
        }

        Reserva reservaExistente = optionalReserva.get();
        reservaExistente.setNome(reservaAtualizada.getNome());
        reservaExistente.setData(reservaAtualizada.getData());
        reservaExistente.setHora(reservaAtualizada.getHora());
        reservaExistente.setHoraFim(reservaAtualizada.getHoraFim());

        try {
            reservaService.atualizar(reservaExistente);
        } catch (IllegalArgumentException e) {
            model.addAttribute("reserva", reservaExistente);
            model.addAttribute("erro", e.getMessage());
            return "atualizar";
        }

        return "redirect:/listagem";
    }

    @GetMapping("/contato")
    public String paginaContato(Model model) {
        model.addAttribute("activePage", "contato");
        return "contato";
    }
}