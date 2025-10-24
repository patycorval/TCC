package com.bd.sitebd.controller;

import com.bd.sitebd.model.*;
import com.bd.sitebd.model.enums.StatusReserva;
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
    private ProfessorRepository professorRepository;
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
        model.addAttribute("professores", professorRepository.findAll());
        model.addAttribute("salas", salaService.listarTodas());
        model.addAttribute("activePage", "grade");
        return "grade";
    }

    @GetMapping("/api/professores")
    @ResponseBody
    public List<Professor> getProfessoresPorCurso(@RequestParam Long cursoId) {
        return professorRepository.findByCursos_Id(cursoId);
    }

    @GetMapping("/api/grade/reservas")
    @ResponseBody
    public List<ReservaDTO> getReservasParaGrade(
            @RequestParam Long cursoId,
            @RequestParam int semestre) {

        // CORREÇÃO APLICADA AQUI:
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
            long professorId = Long.parseLong(payload.get("professorId"));
            long salaId = Long.parseLong(payload.get("salaId"));
            String diaSemanaStr = payload.get("diaSemana");
            String horarioStr = payload.get("horario");

            Optional<Professor> profOpt = professorRepository.findById(professorId);
            Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
            Sala sala = salaService.buscarPorId(salaId);

            if (profOpt.isEmpty() || cursoOpt.isEmpty() || sala == null) {
                return ResponseEntity.badRequest().body("Professor, Curso ou Sala não encontrado.");
            }
            Professor professor = profOpt.get();
            Curso curso = cursoOpt.get();

            int mesInicio = LocalDate.now().getMonthValue() >= 8 ? 8 : 2;
            LocalDate inicioSemestre = LocalDate.now().withMonth(mesInicio).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fimSemestre = inicioSemestre.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth());

            String[] horarios = horarioStr.split(" às ");
            LocalTime horaInicio = LocalTime.parse(horarios[0]);
            LocalTime horaFim = LocalTime.parse(horarios[1]);

            // Mapeia string do dia para o Enum DayOfWeek
            DayOfWeek diaOfWeek = DayOfWeek.MONDAY;
            try {

                diaOfWeek = DayOfWeek.valueOf(diaSemanaStr.toUpperCase()
                        .replace("Ç", "C").replace("Á", "A")
                        .replace("Ê", "E").replace("É", "E")); // Handle TERÇA, SÁBADO etc.
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao mapear dia da semana: " + diaSemanaStr);

            }

            LocalDate dataAtual = inicioSemestre.with(TemporalAdjusters.nextOrSame(diaOfWeek));

            while (!dataAtual.isAfter(fimSemestre)) {
                Reserva r = new Reserva();
                r.setNumero(sala.getNumero());
                r.setNome(professor.getNome());
                r.setEmailRequisitor(professor.getEmail());
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

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao salvar: " + e.getMessage());
        }
    }

    // DTO Interno para retornar dados da reserva para o frontend
    public static class ReservaDTO {
        public String diaSemana;
        public String horario;
        public String professorNome;
        public String salaNumero;

        public ReservaDTO(Reserva reserva) {
            this.diaSemana = reserva.getData().getDayOfWeek().name(); // MONDAY, TUESDAY etc.
            this.horario = String.format("%02d:%02d às %02d:%02d",
                    reserva.getHora().getHour(), reserva.getHora().getMinute(),
                    reserva.getHoraFim().getHour(), reserva.getHoraFim().getMinute());
            this.professorNome = reserva.getNome();
            this.salaNumero = reserva.getNumero();
        }
    }
}