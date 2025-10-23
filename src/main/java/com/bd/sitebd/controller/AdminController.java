package com.bd.sitebd.controller;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.Usuario;
import com.bd.sitebd.model.dto.DiaCalendario;
import com.bd.sitebd.model.enums.StatusReserva;
import org.springframework.security.core.Authentication;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.service.DiaBloqueadoService;
import com.bd.sitebd.service.ReservaService;
import com.bd.sitebd.service.UsuarioService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DiaBloqueadoService diaBloqueadoService;

    // Tela de cadastro de usuários (apenas ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cadastro")
    public String exibirCadastro() {
        return "cadastro";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastro")
    public String processarCadastro(@RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            @RequestParam TipoUsuario tipo,
            RedirectAttributes redirectAttributes) {

        if (!senha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não conferem!");
            return "redirect:/admin/cadastro";
        }

        try {
            Usuario novo = new Usuario();
            novo.setEmail(email);
            novo.setSenha(passwordEncoder.encode(senha));
            novo.setTipo(tipo);

            usuarioService.salvar(novo);

            redirectAttributes.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/admin/cadastro";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auditorio-admin")
    public String auditorioAdmin(@RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            Model model) {

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String emailUsuario = authentication.getName();
        model.addAttribute("usuarioLogadoEmail", emailUsuario);

        // Lógica completa copiada do UsuarioController para manter a consistência
        YearMonth ym = (ano != null && mes != null) ? YearMonth.of(ano, mes) : YearMonth.now();

        List<LocalDate> diasBloqueados = diaBloqueadoService.buscarDiasBloqueadosNoMes(ym);

        YearMonth mesCorrente = YearMonth.now();
        model.addAttribute("desabilitarAnterior", !ym.isAfter(mesCorrente));

        // Para o admin, buscamos TODAS as reservas do auditório, não apenas as de um
        // usuário
        List<Reserva> reservasAuditorio = reservaService.buscarReservasAuditorio(ym);

        List<DiaCalendario> diasDoMes = new ArrayList<>();
        LocalDate primeiroDiaDoMes = ym.atDay(1);
        int diaDaSemanaDoPrimeiroDia = primeiroDiaDoMes.getDayOfWeek().getValue();

        for (int i = 1; i < diaDaSemanaDoPrimeiroDia; i++) {
            diasDoMes.add(new DiaCalendario(0, "vazio"));
        }

        LocalDate hoje = LocalDate.now();

        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate dataDoDia = ym.atDay(i);
            DiaCalendario diaObj;

            // ----- INÍCIO DA LÓGICA CORRIGIDA -----
            if (dataDoDia.isBefore(hoje)) {
                diaObj = new DiaCalendario(i, "passado");
            } else {
                // Para dias futuros ou o dia de hoje
                boolean foiBloqueadoPeloAdmin = diasBloqueados.contains(dataDoDia);
                boolean ehDomingo = dataDoDia.getDayOfWeek() == DayOfWeek.SUNDAY;

                if (foiBloqueadoPeloAdmin) {
                    diaObj = new DiaCalendario(i, "bloqueado"); // Status para dias bloqueados
                } else if (ehDomingo) {
                    diaObj = new DiaCalendario(i, "indisponivel"); // Status para domingos
                } else {
                    diaObj = new DiaCalendario(i, "disponivel"); // Status para dias normais

                    // Adiciona os eventos do dia (reservas)
                    List<Reserva> eventosDoDia = reservasAuditorio.stream()
                            .filter(r -> r.getData().isEqual(dataDoDia))
                            .sorted(Comparator.comparing(Reserva::getHora))
                            .toList();
                    diaObj.setEventos(eventosDoDia);
                }
            }
            // ----- FIM DA LÓGICA CORRIGIDA -----

            diasDoMes.add(diaObj);
        }

        model.addAttribute("diasDoMes", diasDoMes);
        model.addAttribute("ym", ym);
        model.addAttribute("proximoMes", ym.plusMonths(1).getMonthValue());
        model.addAttribute("proximoAno", ym.plusMonths(1).getYear());
        model.addAttribute("anteriorMes", ym.minusMonths(1).getMonthValue());
        model.addAttribute("anteriorAno", ym.minusMonths(1).getYear());
        model.addAttribute("activePage", "auditorio");

        return "auditorio-admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/solicitacoes")
    public String exibirSolicitacoes(Model model) {
        model.addAttribute("solicitacoes", reservaService.buscarPorStatus(StatusReserva.PENDENTE));
        model.addAttribute("activePage", "solicitacoes");
        return "solicitacoes-admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/solicitacoes/atualizar")
    public String atualizarSolicitacao(@RequestParam("id") Long id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            StatusReserva novoStatus = StatusReserva.valueOf(status);
            reservaService.atualizarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status da reserva atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar o status da reserva.");
        }
        return "redirect:/admin/solicitacoes";
    }

    // Endpoint para BLOQUEAR
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorio/bloquear-dias")
    public String bloquearDias(
            @RequestParam(value = "diasSelecionados", required = false) Set<LocalDate> diasParaBloquear,
            RedirectAttributes redirectAttributes) {
        if (diasParaBloquear == null || diasParaBloquear.isEmpty()) {
            return "redirect:/admin/auditorio-admin"; // Redireciona se nada for selecionado
        }
        try {
            diasParaBloquear.forEach(diaBloqueadoService::bloquearDia);
            // Mensagem de sucesso removida
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao bloquear os dias.");
        }
        YearMonth ym = YearMonth.from(diasParaBloquear.iterator().next());
        return "redirect:/admin/auditorio-admin?mes=" + ym.getMonthValue() + "&ano=" + ym.getYear();
    }

    // NOVO Endpoint para DESBLOQUEAR
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorio/desbloquear-dias")
    public String desbloquearDias(
            @RequestParam(value = "diasSelecionados", required = false) Set<LocalDate> diasParaDesbloquear,
            RedirectAttributes redirectAttributes) {
        if (diasParaDesbloquear == null || diasParaDesbloquear.isEmpty()) {
            return "redirect:/admin/auditorio-admin"; // Redireciona se nada for selecionado
        }
        try {
            diasParaDesbloquear.forEach(diaBloqueadoService::desbloquearDia);
            // Mensagem de sucesso removida
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao desbloquear os dias.");
        }
        YearMonth ym = YearMonth.from(diasParaDesbloquear.iterator().next());
        return "redirect:/admin/auditorio-admin?mes=" + ym.getMonthValue() + "&ano=" + ym.getYear();
    }

}
