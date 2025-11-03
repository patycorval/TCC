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
        model.addAttribute("cursos", cursoRepository.findAll());
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

    // Dentro de GradeController.java

    @GetMapping("/api/grade/reservas")
    @ResponseBody
    public List<ReservaDTO> getReservasParaGrade(
            @RequestParam Long cursoId,
            @RequestParam int semestre) {

        System.out.println("==========================================================");
        System.out.println("[DEBUG GET /api/grade/reservas]");
        System.out.println("Buscando grade para Curso ID: " + cursoId + ", Semestre: " + semestre);

        // --- NOVA LÓGICA DE BUSCA (MAIS ROBUSTA) ---
        // 1. Busca TODAS as reservas que são da GRADE para este CURSO e SEMESTRE
        List<Reserva> todasAsReservasDaGrade = reservaRepository.findByCursoIdAndSemestreAndGradeReservaTrue(
                cursoId, semestre);

        System.out.println("Reservas da grade encontradas no banco (total): " + todasAsReservasDaGrade.size());

        // 2. Filtra para obter apenas UMA reserva para cada slot (dia/horário)
        List<Reserva> gradeUnica = todasAsReservasDaGrade.stream()
                .collect(Collectors.groupingBy(
                        // Agrupa por uma chave combinada de Dia da Semana + Horário de Início
                        r -> r.getData().getDayOfWeek().toString() + "_" + r.getHora().toString(),
                        // Pega apenas o primeiro item de cada grupo (a reserva com a data mais antiga)
                        Collectors.collectingAndThen(Collectors.minBy(Comparator.comparing(Reserva::getData)),
                                Optional::get)))
                .values().stream().toList(); // Pega os valores do mapa (a reserva única de cada slot)

        System.out.println("Retornando " + gradeUnica.size() + " slots únicos para o frontend.");
        System.out.println("==========================================================");

        // 3. Converte para DTO e retorna
        return gradeUnica.stream().map(ReservaDTO::new).collect(Collectors.toList());
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
            LocalDate inicioSemestre = LocalDate.now().withMonth(mesInicio).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fimSemestre = inicioSemestre.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth());

            String[] horarios = horarioStr.split(" às ");
            LocalTime horaInicio = LocalTime.parse(horarios[0]);
            LocalTime horaFim = LocalTime.parse(horarios[1]);

            // --- CORREÇÃO APLICADA AQUI ---
            // Mapear String do dia para DayOfWeek usando switch case
            DayOfWeek diaOfWeek = DayOfWeek.MONDAY; // Padrão seguro
            switch (diaSemanaStr) {
                case "Segunda":
                    diaOfWeek = DayOfWeek.MONDAY;
                    break;
                case "Terça":
                    diaOfWeek = DayOfWeek.TUESDAY;
                    break;
                case "Quarta":
                    diaOfWeek = DayOfWeek.WEDNESDAY;
                    break;
                case "Quinta":
                    diaOfWeek = DayOfWeek.THURSDAY;
                    break;
                case "Sexta":
                    diaOfWeek = DayOfWeek.FRIDAY;
                    break;
                case "Sábado":
                    diaOfWeek = DayOfWeek.SATURDAY;
                    break;
                default:
                    // Loga o erro e retorna bad request se o dia for inválido
                    System.err.println("Dia da semana inválido recebido no payload: " + diaSemanaStr);
                    return ResponseEntity.badRequest().body("Dia da semana inválido: " + diaSemanaStr);
            }
            // --- FIM DA CORREÇÃO ---

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

    public static class ReservaDTO {
        public String diaSemana;
        public String horario;
        public String professorNome;
        public String salaNumero;

        public ReservaDTO(Reserva reserva) {
            this.diaSemana = reserva.getData().getDayOfWeek().name();
            this.horario = String.format("%02d:%02d às %02d:%02d",
                    reserva.getHora().getHour(), reserva.getHora().getMinute(),
                    reserva.getHoraFim().getHour(), reserva.getHoraFim().getMinute());
            this.professorNome = reserva.getNome();
            this.salaNumero = reserva.getNumero();
        }
    }
}