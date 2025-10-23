package com.bd.sitebd.controller;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // Exibir formulário de reserva de sala
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservar")
    public String exibirFormulario(@RequestParam String numero, Model model) {
        Reserva reserva = new Reserva();
        reserva.setNumero(numero);
        model.addAttribute("reserva", reserva);
        // Adiciona o atributo com valor 'false' para que a página sempre o encontre.
        model.addAttribute("reservaEfetuada", false);

        return "reservar";
    }

    // Salvar reserva de sala (agora com status APROVADA por padrão)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservar")
    public String realizarReserva(@ModelAttribute Reserva reserva, Model model, Authentication authentication) {
        try {
            reserva.setEmailRequisitor(authentication.getName());
            reserva.setNome(authentication.getName());
            // reserva.setStatus(StatusReserva.APROVADA); // Reservas de sala são aprovadas
            // diretamente
            reservaService.salvar(reserva);

            model.addAttribute("reservaEfetuada", true);

            Reserva novaReserva = new Reserva();
            novaReserva.setNumero(reserva.getNumero());
            model.addAttribute("reserva", novaReserva);

            return "reservar";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("reserva", reserva);
            return "reservar";
        }
    }

    // Listar as reservas do usuário logado
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listagem")
    public String listarReservas(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        List<Reserva> todasAsReservas = reservaService.listarPorUsuario(emailUsuario);

        List<Reserva> reservasSalas = todasAsReservas.stream()
                .filter(r -> !"Auditorio".equalsIgnoreCase(r.getNumero()))
                .toList();

        List<Reserva> reservasAuditorio = todasAsReservas.stream()
                .filter(r -> "Auditorio".equalsIgnoreCase(r.getNumero()))
                .toList();

        model.addAttribute("reservasSalas", reservasSalas);
        model.addAttribute("reservasAuditorio", reservasAuditorio);
        model.addAttribute("activePage", "listagem");

        return "listagem";
    }

    // Deletar reserva
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deletar/{id}")
    public String deletarReserva(@PathVariable Long id) {
        reservaService.deletar(id);
        return "redirect:/listagem";
    }

    // Exibir formulário de edição de reserva
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservas/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        Optional<Reserva> optionalReserva = reservaService.buscarPorId(id);
        if (optionalReserva.isEmpty()) {
            return "redirect:/listagem";
        }

        Reserva reserva = optionalReserva.get();
        model.addAttribute("reserva", reserva);
        return "atualizar";
    }

    // Atualizar reserva
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

    // Página de contato (acesso livre)
    @GetMapping("/contato")
    public String paginaContato(Model model) {
        model.addAttribute("activePage", "contato");
        return "contato";
    }
}