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

    // LOGIN (público)
    @GetMapping("/login")
    public String exibirLogin() {
        return "login";
    }

    // ROTAS DO SISTEMA (todos logados)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String principal(Model model) {
        List<Sala> todasSalas = salaService.listarTodas();

        List<Sala> salasAndar3 = todasSalas.stream()
                .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().contains("3"))
                .toList();

        List<Sala> salasAndar5 = todasSalas.stream()
                .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().contains("5"))
                .toList();

        model.addAttribute("salasAndar3", salasAndar3);
        model.addAttribute("salasAndar5", salasAndar5);
        model.addAttribute("activePage", "principal");

        return "principal";
    }

    // Auditório: só ADMIN e PROFESSOR
    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @GetMapping("/auditorio")
    public String auditorio(Model model) {
        YearMonth ym = YearMonth.now();
        List<DiaCalendario> diasDoMes = new ArrayList<>();
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            String status = "disponivel";
            if (i == 5 || i == 12 || i == 20)
                status = "evento";
            else if (i == 8 || i == 15)
                status = "indisponivel";
            diasDoMes.add(new DiaCalendario(i, status));
        }

        model.addAttribute("diasDoMes", diasDoMes);
        model.addAttribute("activePage", "auditorio");
        return "auditorio";
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @PostMapping("/auditorio/solicitar")
    public String solicitarReservaAuditorio(@RequestParam("nomeRequisitor") String nomeRequisitor,
            @RequestParam("eventoProposto") String eventoProposto,
            @RequestParam("dataEvento") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataEvento,
            @RequestParam("horaEvento") String horaEvento,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Reserva novaSolicitacao = new Reserva();
        novaSolicitacao.setNome(nomeRequisitor);
        novaSolicitacao.setEvento(eventoProposto);
        novaSolicitacao.setNumero("Auditorio");
        novaSolicitacao.setEmailRequisitor(emailUsuario);
        novaSolicitacao.setData(dataEvento);
        novaSolicitacao.setHora(LocalTime.parse(horaEvento));
        novaSolicitacao.setStatus(StatusReserva.PENDENTE);

        reservaService.salvar(novaSolicitacao);

        model.addAttribute("mensagemSucesso", "Sua solicitação foi enviada e está aguardando aprovação.");
        model.addAttribute("activePage", "auditorio");

        YearMonth ym = YearMonth.now();
        List<DiaCalendario> diasDoMes = new ArrayList<>();
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            String status = "disponivel";
            if (i == 5 || i == 12 || i == 20)
                status = "evento";
            else if (i == 8 || i == 15)
                status = "indisponivel";
            diasDoMes.add(new DiaCalendario(i, status));
        }

        model.addAttribute("diasDoMes", diasDoMes);
        return "auditorio";
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @GetMapping("/grade")
    public String grade(Model model) {
        model.addAttribute("activepage", "grade");
        return "grade";
    }
}