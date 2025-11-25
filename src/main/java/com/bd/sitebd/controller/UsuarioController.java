package com.bd.sitebd.controller;

import com.bd.sitebd.model.Sala;
import com.bd.sitebd.service.SalaService;
import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.service.DiaBloqueadoService;
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
import java.util.Comparator;
import java.util.List;

@Controller
public class UsuarioController {
    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DiaBloqueadoService diaBloqueadoService;

    @GetMapping("/login")
    public String exibirLogin() {
        return "login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String principal(
            @RequestParam(required = false) String andar,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) String tiposala,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFiltro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicioFiltro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFimFiltro,
            Model model) {

        List<Sala> salasFiltradas = salaService.getSalasFiltradas(andar, recurso, tiposala, dataFiltro,
                horaInicioFiltro, horaFimFiltro);

        List<Sala> salasTerreo = salasFiltradas.stream()
                .filter(s -> s.getLocalizacao() != null &&
                        (s.getLocalizacao().equalsIgnoreCase("Térreo") || s.getLocalizacao().startsWith("0")))
                .toList();

        List<Sala> salasAndar2 = salasFiltradas.stream()
                .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().startsWith("2"))
                .toList();

        List<Sala> salasAndar3 = salasFiltradas.stream()
                .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().startsWith("3"))
                .toList();

        List<Sala> salasAndar5 = salasFiltradas.stream()
                .filter(s -> s.getLocalizacao() != null && s.getLocalizacao().startsWith("5"))
                .toList();

        model.addAttribute("salasTerreo", salasTerreo);
        model.addAttribute("salasAndar2", salasAndar2);
        model.addAttribute("salasAndar3", salasAndar3);
        model.addAttribute("salasAndar5", salasAndar5);
        model.addAttribute("activePage", "principal");
        model.addAttribute("andarSelecionado", andar);
        model.addAttribute("recursoSelecionado", recurso);
        model.addAttribute("tipoSalaSelecionado", tiposala);
        model.addAttribute("dataFiltro", dataFiltro);
        model.addAttribute("horaInicioFiltro", horaInicioFiltro);
        model.addAttribute("horaFimFiltro", horaFimFiltro);

        return "principal";
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESSOR')")
    @GetMapping("/auditorio")
    public String auditorio(@RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        model.addAttribute("usuarioLogadoEmail", emailUsuario);

        YearMonth ym = (ano != null && mes != null) ? YearMonth.of(ano, mes) : YearMonth.now();
        YearMonth mesCorrente = YearMonth.now();
        model.addAttribute("desabilitarAnterior", !ym.isAfter(mesCorrente));

        List<LocalDate> diasBloqueados = diaBloqueadoService.buscarDiasBloqueadosNoMes(ym);
        List<Reserva> reservasAuditorio = reservaService.buscarReservasAuditorioParaUsuario(ym, emailUsuario);

        reservasAuditorio.forEach(reserva -> {
            String periodoIdeal = reservaService.determinarPeriodoParaData(reserva.getData());
            reserva.setPeriodoIdeal(periodoIdeal);
        });

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

            if (dataDoDia.isBefore(hoje)) {
                diaObj = new DiaCalendario(i, "passado");
            } else if (diasBloqueados.contains(dataDoDia) || dataDoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                // Se o dia estiver na lista de bloqueados OU for um domingo, fica indisponível
                diaObj = new DiaCalendario(i, "indisponivel");
            } else {
                diaObj = new DiaCalendario(i, "disponivel");
                List<Reserva> eventosDoDia = reservasAuditorio.stream()
                        .filter(r -> r.getData().isEqual(dataDoDia))
                        .sorted(Comparator.comparing(Reserva::getHora))
                        .toList();
                diaObj.setEventos(eventosDoDia);
            }
            diasDoMes.add(diaObj);

        }

        model.addAttribute("diasDoMes", diasDoMes);
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

            reservaService.salvar(novaSolicitacao);

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Reserva do auditório confirmada com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("mensagemSucesso",
                        "Sua solicitação foi enviada e está aguardando aprovação.");
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
       
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/auditorio-admin?mes=" + dataEvento.getMonthValue() + "&ano=" + dataEvento.getYear();
        } else {
            return "redirect:/auditorio?mes=" + dataEvento.getMonthValue() + "&ano=" + dataEvento.getYear();
        }

    }
}
