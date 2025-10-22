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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GradeController {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private SalaService salaService;
    @Autowired private ReservaService reservaService;

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
    
    // API INTERNA: Busca as reservas já existentes para popular a grade
    @GetMapping("/api/grade/reservas")
    @ResponseBody
    public List<Reserva> getReservasParaGrade(
            @RequestParam String cursoSigla,
            @RequestParam String periodo,
            @RequestParam int semestre
    ) {
        return new ArrayList<>(); 
    }

    // Salva a grade inteira como reservas
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/grade/salvar-semestre")
    @ResponseBody
    public ResponseEntity<?> salvarGradeComoReservas(@RequestBody Map<String, String> payload) {
        try {
            String cursoSigla = payload.get("cursoSigla");
            String periodo = payload.get("periodo");
            // int semestre = Integer.parseInt(payload.get("semestre"));
            long professorId = Long.parseLong(payload.get("professorId"));
            long salaId = Long.parseLong(payload.get("salaId"));
            String diaSemanaStr = payload.get("diaSemana");
            String horarioStr = payload.get("horario");

            // Encontrar os objetos
            Optional<Professor> profOpt = professorRepository.findById(professorId);
            Sala sala = salaService.buscarPorId(salaId);

            if (profOpt.isEmpty() || sala == null) {
                return ResponseEntity.badRequest().body("Professor ou Sala não encontrado.");
            }
            Professor professor = profOpt.get();

            // Definir datas do semestre (AJUSTE CONFORME NECESSÁRIO)
            LocalDate inicioSemestre = LocalDate.now().withMonth(8).withDayOfMonth(1); 
            LocalDate fimSemestre = LocalDate.now().withMonth(12).withDayOfMonth(20); 

            // Pegar horários
            String[] horarios = horarioStr.split(" às ");
            LocalTime horaInicio = LocalTime.parse(horarios[0]);
            LocalTime horaFim = LocalTime.parse(horarios[1]);
            
            // Mapear String do dia para DayOfWeek
            DayOfWeek diaOfWeek = DayOfWeek.MONDAY; // Padrão
            switch (diaSemanaStr) {
                case "Segunda": diaOfWeek = DayOfWeek.MONDAY; break;
                case "Terça":   diaOfWeek = DayOfWeek.TUESDAY; break;
                case "Quarta":  diaOfWeek = DayOfWeek.WEDNESDAY; break;
                case "Quinta":  diaOfWeek = DayOfWeek.THURSDAY; break;
                case "Sexta":   diaOfWeek = DayOfWeek.FRIDAY; break;
                case "Sábado":  diaOfWeek = DayOfWeek.SATURDAY; break;
            }

            // Criar reservas para todo o semestre
            LocalDate dataAtual = inicioSemestre.with(TemporalAdjusters.nextOrSame(diaOfWeek));
            
            while (dataAtual.isBefore(fimSemestre)) {
                Reserva r = new Reserva();
                r.setNumero(sala.getNumero()); // Usa o número da sala
                r.setNome(professor.getNome()); // Nome do professor
                r.setEmailRequisitor(professor.getEmail()); // Email do professor
                r.setData(dataAtual);
                r.setHora(horaInicio);
                r.setHoraFim(horaFim);
                r.setStatus(StatusReserva.APROVADA); // Aprovada direto
                
                try {
                    reservaService.salvar(r); // USA O SEU SERVICE! (já checa conflitos)
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
}