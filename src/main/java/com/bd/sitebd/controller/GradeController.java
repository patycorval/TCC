package com.bd.sitebd.controller;

import com.bd.sitebd.model.*;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.repositories.*;
import com.bd.sitebd.service.ReservaService;
import com.bd.sitebd.service.SalaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class GradeController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/grade")
    public String grade(Model model) {
        List<Curso> cursos = cursoRepository.findAll(Sort.by("sigla").ascending().and(Sort.by("periodo").ascending()));
        model.addAttribute("cursos", cursos);
        model.addAttribute("professores", usuarioRepository.findByTipoIn(
                List.of(TipoUsuario.PROFESSOR, TipoUsuario.MONITOR)));
        model.addAttribute("salas", salaService.listarTodas());
        model.addAttribute("activePage", "grade");
        return "grade";
    }

    @GetMapping("/api/professores")
    @ResponseBody
    public List<Usuario> getProfessoresPorCurso(@RequestParam Long cursoId) {
        return usuarioRepository.findByCursos_IdAndTipoIn(cursoId,
                List.of(TipoUsuario.PROFESSOR, TipoUsuario.MONITOR));
    }

    @GetMapping("/api/grade/reservas")
    @ResponseBody
    public List<ReservaDTO> getReservasParaGrade(
            @RequestParam Long cursoId,
            @RequestParam int semestre) {

        System.out.println("==========================================================");
        System.out.println("[DEBUG GET /api/grade/reservas]");
        System.out.println("Buscando grade para Curso ID: " + cursoId + ", Semestre: " + semestre);

        List<Reserva> todasAsReservasDaGrade = reservaRepository.findByCursoIdAndSemestreAndGradeReservaTrue(
                cursoId, semestre);

        System.out.println("Reservas da grade encontradas no banco (total): " + todasAsReservasDaGrade.size());

        List<Reserva> gradeUnica = todasAsReservasDaGrade.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getData().getDayOfWeek().toString() + "_" + r.getHora().toString(),
                        Collectors.collectingAndThen(Collectors.minBy(Comparator.comparing(Reserva::getData)),
                                Optional::get)))
                .values().stream().toList();

        System.out.println("Retornando " + gradeUnica.size() + " slots únicos para o frontend.");
        System.out.println("==========================================================");

        return gradeUnica.stream()
                .map(reserva -> new ReservaDTO(reserva, salaService))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/grade/salvar-semestre")
    @ResponseBody
    public ResponseEntity<?> salvarGradeComoReservas(@RequestBody Map<String, String> payload) {
        try {
            long cursoId = Long.parseLong(payload.get("cursoId"));
            int semestreInt = Integer.parseInt(payload.get("semestre"));
            long usuarioId = Long.parseLong(payload.get("usuarioId"));
            long salaId = Long.parseLong(payload.get("salaId"));
            String diaSemanaStr = payload.get("diaSemana");
            String horarioStr = payload.get("horario");

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
            Sala sala = salaService.buscarPorId(salaId);

            if (usuarioOpt.isEmpty() || cursoOpt.isEmpty() || sala == null) {
                return ResponseEntity.badRequest().body("Usuário (Professor/Monitor), Curso ou Sala não encontrado.");
            }
            Usuario usuario = usuarioOpt.get();
            Curso curso = cursoOpt.get();

            if (usuario.getTipo() != TipoUsuario.PROFESSOR && usuario.getTipo() != TipoUsuario.MONITOR) {
                return ResponseEntity.badRequest().body("Usuário selecionado não é um Professor ou Monitor válido.");
            }

            int mesInicio = LocalDate.now().getMonthValue() >= 8 ? 8 : 2;

            int mesesParaAdicionar = (mesInicio == 8) ? 4 : 5;

            LocalDate inicioSemestre = LocalDate.now().withMonth(mesInicio).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fimSemestre = inicioSemestre.plusMonths(mesesParaAdicionar)
                    .with(TemporalAdjusters.lastDayOfMonth());

            String[] horarios = horarioStr.split(" às ");
            LocalTime horaInicio = LocalTime.parse(horarios[0]);
            LocalTime horaFim = LocalTime.parse(horarios[1]);

            DayOfWeek diaOfWeek;
            switch (diaSemanaStr) {
                case "Segunda" -> diaOfWeek = DayOfWeek.MONDAY;
                case "Terça" -> diaOfWeek = DayOfWeek.TUESDAY;
                case "Quarta" -> diaOfWeek = DayOfWeek.WEDNESDAY;
                case "Quinta" -> diaOfWeek = DayOfWeek.THURSDAY;
                case "Sexta" -> diaOfWeek = DayOfWeek.FRIDAY;
                case "Sábado" -> diaOfWeek = DayOfWeek.SATURDAY;
                default -> {
                    System.err.println("Dia da semana inválido recebido no payload: " + diaSemanaStr);
                    return ResponseEntity.badRequest().body("Dia da semana inválido: " + diaSemanaStr);
                }
            }

            LocalDate dataAtual = inicioSemestre.with(TemporalAdjusters.nextOrSame(diaOfWeek));

            while (!dataAtual.isAfter(fimSemestre)) {
                Reserva r = new Reserva();
                r.setNumero(sala.getNumero());
                String nomeParaReserva = (usuario.getNome() != null && !usuario.getNome().isBlank())
                        ? usuario.getNome()
                        : usuario.getEmail();
                r.setNome(nomeParaReserva);
                r.setEmailRequisitor(usuario.getEmail());
                r.setData(dataAtual);
                r.setHora(horaInicio);
                r.setHoraFim(horaFim);
                r.setStatus(StatusReserva.APROVADA);
                r.setCurso(curso);
                r.setSemestre(semestreInt);
                r.setGradeReserva(true);

                try {
                    reservaService.salvar(r);
                } catch (IllegalArgumentException e) {
                    System.out.println("Conflito ignorado em " + dataAtual + ": " + e.getMessage());
                }

                dataAtual = dataAtual.plusWeeks(1);
            }

            return ResponseEntity.ok().build();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro: ID inválido recebido.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro interno ao salvar: " + e.getMessage());
        }
    }

    // Classe DTO interna corrigida
    public static class ReservaDTO {
        public String diaSemana;
        public String horario;
        public String professorNome;
        public String salaNomeCompleto;

        public ReservaDTO(Reserva reserva, SalaService salaService) {
            this.diaSemana = reserva.getData().getDayOfWeek().name();
            this.horario = String.format("%02d:%02d às %02d:%02d",
                    reserva.getHora().getHour(), reserva.getHora().getMinute(),
                    reserva.getHoraFim().getHour(), reserva.getHoraFim().getMinute());
            this.professorNome = reserva.getNome();

            // Busca sala correspondente
            Sala salaEncontrada = salaService.listarTodas().stream()
                    .filter(s -> s.getNumero().equals(reserva.getNumero()))
                    .findFirst()
                    .orElse(null);

            if (salaEncontrada != null) {
                String tipo = salaEncontrada.getTipoSalaDisplayName();
                String numero = salaEncontrada.getNumero();

                // 🔧 Correção: evita duplicar o nome do tipo da sala
                if (numero.toLowerCase().contains(tipo.toLowerCase())) {
                    this.salaNomeCompleto = numero.trim();
                } else {
                    this.salaNomeCompleto = tipo + " " + numero;
                }
            } else {
                this.salaNomeCompleto = reserva.getNumero(); // fallback
            }
        }
    }
}
