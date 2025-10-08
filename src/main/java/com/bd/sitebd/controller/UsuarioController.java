package com.bd.sitebd.controller;

import com.bd.sitebd.model.Sala;
import com.bd.sitebd.service.SalaService;
import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.service.ReservaService;
import com.bd.sitebd.model.dto.DiaCalendario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UsuarioController {
    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/login")
    public String exibirLogin() {
        return "login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String principal(
            // O método agora aceita os parâmetros de filtro da URL
            @RequestParam(required = false) String andar,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) String tiposala,
            Model model) {

        // busca e filtra as salas com base nos parâmetros recebidos
        List<Sala> salasFiltradas = salaService.getSalasFiltradas(andar, recurso, tiposala);

        List<Sala> salasAndar3 = salasFiltradas.stream()
        .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().startsWith("3"))
        .toList();

        List<Sala> salasAndar5 = salasFiltradas.stream()
        .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().startsWith("5"))
        .toList();

        model.addAttribute("salasAndar3", salasAndar3);
        model.addAttribute("salasAndar5", salasAndar5);
        model.addAttribute("activePage", "principal");

        return "principal";
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @GetMapping("/auditorio")
    public String auditorio(@RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            Model model) {
        YearMonth ym = (ano != null && mes != null) ? YearMonth.of(ano, mes) : YearMonth.now();
        List<Reserva> reservasAuditorio = reservaService.buscarReservasAuditorio(ym);

        List<DiaCalendario> diasDoMes = new ArrayList<>();
        LocalDate primeiroDiaDoMes = ym.atDay(1);
        int diaDaSemanaDoPrimeiroDia = primeiroDiaDoMes.getDayOfWeek().getValue();
        System.out
                .println("!!!!!!!!!!!!O valor do dia da semana do primeiro dia do mes é: " + diaDaSemanaDoPrimeiroDia);
        for (int i = 1; i < diaDaSemanaDoPrimeiroDia; i++) {
            diasDoMes.add(new DiaCalendario(0, "vazio"));
        }

        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate dataDoDia = ym.atDay(i);

            if (dataDoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                diasDoMes.add(new DiaCalendario(i, "indisponivel"));
            } else {
                boolean temEvento = reservasAuditorio.stream().anyMatch(r -> r.getData().isEqual(dataDoDia));
                String status = temEvento ? "evento" : "disponivel";
                diasDoMes.add(new DiaCalendario(i, status));
            }
        }

        model.addAttribute("diasDoMes", diasDoMes);
        model.addAttribute("reservasAuditorio", reservasAuditorio);
        model.addAttribute("ym", ym);
        model.addAttribute("proximoMes", ym.plusMonths(1).getMonthValue());
        model.addAttribute("proximoAno", ym.plusMonths(1).getYear());
        model.addAttribute("anteriorMes", ym.minusMonths(1).getMonthValue());
        model.addAttribute("anteriorAno", ym.minusMonths(1).getYear());
        model.addAttribute("activePage", "auditorio");

        return "auditorio";
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @PostMapping("/auditorio/solicitar")
    public String solicitarReservaAuditorio(
            @RequestParam("nomeRequisitor") String nomeRequisitor,
            @RequestParam("eventoProposto") String eventoProposto,
            @RequestParam("dataEvento") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataEvento,
            @RequestParam("horaEvento") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaEvento,
            @RequestParam("horaFimEvento") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFimEvento,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailUsuario = authentication.getName();

            Reserva novaSolicitacao = new Reserva();
            novaSolicitacao.setNome(nomeRequisitor);
            novaSolicitacao.setEvento(eventoProposto);
            novaSolicitacao.setNumero("Auditorio");
            novaSolicitacao.setEmailRequisitor(emailUsuario);
            novaSolicitacao.setData(dataEvento);
            novaSolicitacao.setHora(horaEvento);
            novaSolicitacao.setHoraFim(horaFimEvento);
            novaSolicitacao.setStatus(StatusReserva.PENDENTE);

            reservaService.salvar(novaSolicitacao);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Sua solicitação foi enviada e está aguardando aprovação.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/auditorio?mes=" + dataEvento.getMonthValue() + "&ano=" + dataEvento.getYear();
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @GetMapping("/grade")
    public String grade(Model model) {
        model.addAttribute("activepage", "grade");
        return "grade";
    }
}