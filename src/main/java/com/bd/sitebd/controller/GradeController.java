package com.bd.sitebd.controller;

import com.bd.sitebd.model.*;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.repositories.*;
import com.bd.sitebd.service.ReservaService;
import com.bd.sitebd.service.SalaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class GradeController {

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository; // ADICIONADO
    @Autowired
    private SalaService salaService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private ReservaRepository reservaRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/grade")
    public String grade(Model model) {
        model.addAttribute("cursos", cursoRepository.findAll());
        model.addAttribute("professores", usuarioRepository.findByTipoIn(
                List.of(TipoUsuario.PROFESSOR, TipoUsuario.MONITOR)));
        model.addAttribute("salas", salaService.listarTodas());
        model.addAttribute("activePage", "grade");
        return "grade";
    }

    // ALTERADO: Busca Usuarios (Prof/Monitor) por Curso
    @GetMapping("/api/professores") // Mantido nome da URL por compatibilidade com JS
    @ResponseBody
    public List<Usuario> getProfessoresPorCurso(@RequestParam Long cursoId) {
        // Busca usuarios daquele curso que são PROFESSOR ou MONITOR
        return usuarioRepository.findByCursos_IdAndTipoIn(cursoId,
                List.of(TipoUsuario.PROFESSOR, TipoUsuario.MONITOR));
    }

    @GetMapping("/api/grade/reservas")
    @ResponseBody
    public List<ReservaDTO> getReservasParaGrade(
            @RequestParam Long cursoId,
            @RequestParam int semestre) {

        int mesInicio = LocalDate.now().getMonthValue() >= 8 ? 8 : 2;
        LocalDate inicioSemestre = LocalDate.now().withMonth(mesInicio).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate inicioBusca = inicioSemestre.with(DayOfWeek.MONDAY);
        LocalDate fimBusca = inicioBusca.plusDays(6);

        List<Reserva> reservasDaSemana = reservaRepository.findByCursoIdAndSemestreAndDataBetweenOrderByDataAscHoraAsc(
                cursoId, semestre, inicioBusca, fimBusca);

        return reservasDaSemana.stream().map(ReservaDTO::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/grade/salvar-semestre")
    @ResponseBody
    public ResponseEntity<?> salvarGradeComoReservas(@RequestBody Map<String, String> payload) {
        try {
            long cursoId = Long.parseLong(payload.get("cursoId"));
            int semestreInt = Integer.parseInt(payload.get("semestre"));
            long usuarioId = Long.parseLong(payload.get("usuarioId")); // ALTERADO: Recebe usuarioId
            long salaId = Long.parseLong(payload.get("salaId"));
            String diaSemanaStr = payload.get("diaSemana");
            String horarioStr = payload.get("horario");

            // ALTERADO: Busca Usuario em vez de Professor
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
            Sala sala = salaService.buscarPorId(salaId);

            if (usuarioOpt.isEmpty() || cursoOpt.isEmpty() || sala == null) {
                return ResponseEntity.badRequest().body("Usuário (Professor/Monitor), Curso ou Sala não encontrado.");
            }
            Usuario usuario = usuarioOpt.get();
            Curso curso = cursoOpt.get();

            // Valida se o usuário selecionado é Professor ou Monitor
            if (usuario.getTipo() != TipoUsuario.PROFESSOR && usuario.getTipo() != TipoUsuario.MONITOR) {
                return ResponseEntity.badRequest().body("Usuário selecionado não é um Professor ou Monitor válido.");
            }

            int mesInicio = LocalDate.now().getMonthValue() >= 8 ? 8 : 2;
            LocalDate inicioSemestre = LocalDate.now().withMonth(mesInicio).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fimSemestre = inicioSemestre.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth());

            String[] horarios = horarioStr.split(" às ");
            LocalTime horaInicio = LocalTime.parse(horarios[0]);
            LocalTime horaFim = LocalTime.parse(horarios[1]);

            DayOfWeek diaOfWeek = DayOfWeek.MONDAY;
            try {
                diaOfWeek = DayOfWeek.valueOf(diaSemanaStr.toUpperCase()
                        .replace("Ç", "C").replace("Á", "A")
                        .replace("Ê", "E").replace("É", "E"));
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao mapear dia da semana: " + diaSemanaStr);
                return ResponseEntity.badRequest().body("Dia da semana inválido: " + diaSemanaStr);
            }

            LocalDate dataAtual = inicioSemestre.with(TemporalAdjusters.nextOrSame(diaOfWeek));

            while (!dataAtual.isAfter(fimSemestre)) {
                Reserva r = new Reserva();
                r.setNumero(sala.getNumero());
                // ALTERADO: Usa dados do Usuario
                r.setNome(usuario.getEmail()); // Usando email como 'nome' da reserva (AJUSTE SE Usuario tiver campo
                                               // 'nome')
                r.setEmailRequisitor(usuario.getEmail()); // Email do usuario (prof/monitor)
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

    // DTO não precisa mudar, já pega nome/email da Reserva
    public static class ReservaDTO {
        public String diaSemana;
        public String horario;
        public String professorNome; // Mantido nome, pega de reserva.getNome()
        public String salaNumero;

        public ReservaDTO(Reserva reserva) {
            this.diaSemana = reserva.getData().getDayOfWeek().name();
            this.horario = String.format("%02d:%02d às %02d:%02d",
                    reserva.getHora().getHour(), reserva.getHora().getMinute(),
                    reserva.getHoraFim().getHour(), reserva.getHoraFim().getMinute());
            this.professorNome = reserva.getNome(); // Pega o nome/email que foi salvo na reserva
            this.salaNumero = reserva.getNumero();
        }
    }
}