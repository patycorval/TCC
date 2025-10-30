package com.bd.sitebd.controller;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            reserva.setGradeReserva(false);
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

    /**
     * MÉTODO CORRIGIDO
     * Agora ele recebe o parâmetro 'periodo' da URL. Se nenhum for passado, ele usa
     * '15dias' como padrão.
     * Isso garante que o filtro enviado pelo calendário seja aplicado.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listagem")
    public String listarReservas(
            @RequestParam(name = "periodo", defaultValue = "15dias") String periodo,
            Model model,
            Authentication authentication) { // Recebe Authentication

        String emailUsuario = authentication.getName();
        // Verifica se o usuário tem a ROLE_ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        List<Reserva> todasAsReservas;

        if (isAdmin) {
            // Se for ADMIN, busca TODAS as reservas (ainda usando o filtro de período, se
            // necessário)
            // Se o filtro de período NÃO se aplica ao Admin, chame
            // reservaService.listarTodas()
            System.out.println("DEBUG: Usuário é ADMIN. Buscando todas as reservas.");
            // Vamos assumir que o admin também quer filtrar por período
            todasAsReservas = reservaService.listarTodasPorPeriodo(periodo); // <--- PRECISAMOS CRIAR ESTE MÉTODO
        } else {
            // Se não for Admin, busca apenas as do usuário logado
            System.out.println("DEBUG: Usuário NÃO é ADMIN. Buscando reservas para: " + emailUsuario);
            todasAsReservas = reservaService.listarPorUsuarioEPeriodo(emailUsuario, periodo);
        }

        // Separa as reservas por tipo (Auditório ou Sala/Lab)
        List<Reserva> reservasAuditorio = todasAsReservas.stream()
                .filter(r -> "Auditorio".equalsIgnoreCase(r.getNumero()))
                .collect(Collectors.toList());

        List<Reserva> reservasSalas = todasAsReservas.stream()
                .filter(r -> r.getNumero() != null && !"Auditorio".equalsIgnoreCase(r.getNumero()))
                .collect(Collectors.toList());

        System.out.println("DEBUG: Total " + todasAsReservas.size() + ", Salas " + reservasSalas.size() + ", Auditório "
                + reservasAuditorio.size());

        model.addAttribute("reservasAuditorio", reservasAuditorio);
        model.addAttribute("reservasSalas", reservasSalas);
        model.addAttribute("activePage", "listagem");
        model.addAttribute("periodoSelecionado", periodo);

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