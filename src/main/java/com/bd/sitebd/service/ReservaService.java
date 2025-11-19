package com.bd.sitebd.service;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

        @Autowired
        private ReservaRepository reservaRepository;

        private static final LocalTime HORA_INICIO_FUNCIONAMENTO = LocalTime.of(7, 30);
        private static final LocalTime HORA_FIM_FUNCIONAMENTO = LocalTime.of(22, 30);

        public boolean temConflito(Reserva novaReserva) {
                List<Reserva> reservasNoMesmoDia = reservaRepository.findByNumeroAndData(novaReserva.getNumero(),
                                novaReserva.getData());
                LocalTime inicioNova = novaReserva.getHora();
                LocalTime fimNova = novaReserva.getHoraFim();
                for (Reserva r : reservasNoMesmoDia) {
                        LocalTime inicioExistente = r.getHora();
                        LocalTime fimExistente = r.getHoraFim();
                        boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
                        if (conflito) {
                                return true;
                        }
                }
                return false;
        }

        public boolean temConflitoAtualizacao(Reserva novaReserva) {
                List<Reserva> reservasNoMesmoDia = reservaRepository.findByNumeroAndData(novaReserva.getNumero(),
                                novaReserva.getData());
                LocalTime inicioNova = novaReserva.getHora();
                LocalTime fimNova = novaReserva.getHoraFim();
                for (Reserva r : reservasNoMesmoDia) {
                        if (novaReserva.getId() != null && r.getId().equals(novaReserva.getId())) {
                                continue;
                        }
                        LocalTime inicioExistente = r.getHora();
                        LocalTime fimExistente = r.getHoraFim();
                        boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
                        if (conflito) {
                                return true;
                        }
                }
                return false;
        }

        public boolean temConflitoComAprovadas(Reserva novaReserva) {
                List<Reserva> reservasAprovadasNoDia = reservaRepository.findByNumeroAndDataAndStatus(
                                novaReserva.getNumero(),
                                novaReserva.getData(),
                                StatusReserva.APROVADA);

                // Pega os horários da nova solicitação
                LocalTime inicioNova = novaReserva.getHora();
                LocalTime fimNova = novaReserva.getHoraFim();

                // Verifica se há algum conflito de horário
                for (Reserva r : reservasAprovadasNoDia) {
                        LocalTime inicioExistente = r.getHora();
                        LocalTime fimExistente = r.getHoraFim();
                        boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
                        if (conflito) {
                                return true;
                        }
                }
                return false;
        }

        public Reserva salvar(Reserva reserva) {
                // Define o fuso horário da Fatec (Santos/São Paulo)
                ZoneId saoPauloZone = ZoneId.of("America/Sao_Paulo");
                LocalDate hojeEmSaoPaulo = LocalDate.now(saoPauloZone);
                LocalTime agoraEmSaoPaulo = LocalTime.now(saoPauloZone);

                if (reserva.getData().isBefore(hojeEmSaoPaulo)) {
                        throw new IllegalArgumentException("Não é possível fazer reservas para datas retroativas.");
                }

                // 3. VALIDAÇÃO DE HORÁRIO RETROATIVO (somente se a data for hoje)
                if (reserva.getData().isEqual(hojeEmSaoPaulo) && reserva.getHora().isBefore(agoraEmSaoPaulo)) {
                        throw new IllegalArgumentException("Não é possível fazer reservas para horários retroativos.");
                }

                if (reserva.getHoraFim().isBefore(reserva.getHora())
                                || reserva.getHoraFim().equals(reserva.getHora())) {
                        throw new IllegalArgumentException("A hora de fim não pode ser menor ou igual à de início.");
                }

                if (reserva.getHora().isBefore(HORA_INICIO_FUNCIONAMENTO) ||
                                reserva.getHoraFim().isAfter(HORA_FIM_FUNCIONAMENTO)) {
                        throw new IllegalArgumentException("O horário da reserva deve ser entre 07:30 e 22:30.");
                }

                Duration duracao = Duration.between(reserva.getHora(), reserva.getHoraFim());
                if (duracao.toMinutes() < 30) {
                        throw new IllegalArgumentException("A reserva deve durar ao menos 30 minutos.");
                }
                if (!"Auditorio".equals(reserva.getNumero()) && temConflito(reserva)) {
                        throw new IllegalArgumentException("Já existe uma reserva para essa sala nesse horário.");
                }

                // Se for o auditório, verifica conflito apenas com reservas APROVADAS
                if ("Auditorio".equals(reserva.getNumero())) {
                        if (temConflitoComAprovadas(reserva)) {
                                throw new IllegalArgumentException(
                                                "Já existe uma reserva aprovada para o auditório neste dia e horário.");
                        }
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if ("Auditorio".equals(reserva.getNumero())) {
                        if (authentication != null && authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                reserva.setStatus(StatusReserva.APROVADA);
                        } else {
                                reserva.setStatus(StatusReserva.PENDENTE);
                        }
                } else {
                        reserva.setStatus(StatusReserva.APROVADA);
                }

                return reservaRepository.save(reserva);
        }

        public Reserva atualizar(Reserva reserva) {

                LocalDate hoje = LocalDate.now();
                LocalTime agora = LocalTime.now();

                if (reserva.getData().isBefore(hoje)) {
                        throw new IllegalArgumentException("Não é possível atualizar reservas para datas retroativas.");
                }
                if (reserva.getData().isEqual(hoje) && reserva.getHora().isBefore(agora)) {
                        throw new IllegalArgumentException(
                                        "Não é possível atualizar reservas para horários retroativos.");
                }

                if (reserva.getHoraFim().isBefore(reserva.getHora())
                                || reserva.getHoraFim().equals(reserva.getHora())) {
                        throw new IllegalArgumentException("A hora de fim não pode ser menor ou igual à de início.");
                }

                if (reserva.getHora().isBefore(HORA_INICIO_FUNCIONAMENTO) ||
                                reserva.getHoraFim().isAfter(HORA_FIM_FUNCIONAMENTO)) {
                        throw new IllegalArgumentException("O horário da reserva deve ser entre 07:30 e 22:30.");
                }

                Duration duracao = Duration.between(reserva.getHora(), reserva.getHoraFim());
                if (duracao.toMinutes() < 30) {
                        throw new IllegalArgumentException("A reserva deve durar ao menos 30 minutos.");
                }
                if (!"Auditorio".equals(reserva.getNumero()) && temConflitoAtualizacao(reserva)) {
                        throw new IllegalArgumentException("Já existe uma reserva para essa sala nesse horário.");
                }
                return reservaRepository.save(reserva);
        }

        public List<Reserva> buscarPorStatus(StatusReserva status) {
                return reservaRepository.findByStatus(status);
        }

        public void atualizarStatus(Long id, StatusReserva novoStatus) {
                Optional<Reserva> reservaOptional = reservaRepository.findById(id);
                if (reservaOptional.isPresent()) {
                        Reserva reserva = reservaOptional.get();
                        reserva.setStatus(novoStatus);
                        reservaRepository.save(reserva);
                }
        }

        public List<Reserva> listarTodas() {
                return reservaRepository.findAll();
        }

        public List<Reserva> listarPorUsuario(String email) {
                return reservaRepository.findByEmailRequisitor(email);
        }

        public Optional<Reserva> buscarPorId(Long id) {
                return reservaRepository.findById(id);
        }

        public void deletar(Long id) {
                reservaRepository.deleteById(id);
        }

        public List<Reserva> buscarReservasAuditorio(YearMonth ym) {
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                return reservaRepository.findByNumeroAndStatusAndDataBetweenOrderByDataAscHoraAsc("Auditorio",
                                StatusReserva.APROVADA, startOfMonth, endOfMonth);
        }

        public List<Reserva> buscarReservasAuditorioParaUsuario(YearMonth ym, String email) {
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                return reservaRepository.findReservasAuditorioParaUsuario("Auditorio", startOfMonth, endOfMonth, email);
        }

        public List<Reserva> buscarReservasAuditorioParaAdmin(YearMonth ym) {
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                return reservaRepository.findByNumeroAndDataBetweenAndStatusNotOrderByDataAscHoraAsc(
                                "Auditorio", startOfMonth, endOfMonth, StatusReserva.REJEITADA);
        }

        public List<Reserva> listarPorUsuarioEPeriodo(String email, String periodo) {
                LocalDate hoje = LocalDate.now();

                switch (periodo) {
                        case "30dias":
                                return reservaRepository.findByEmailRequisitorAndDataBetweenOrderByDataAsc(email, hoje,
                                                hoje.plusDays(30));
                        case "proximas":
                                return reservaRepository.findByEmailRequisitorAndDataGreaterThanEqualOrderByDataAsc(
                                                email, hoje);
                        case "anteriores":
                                return reservaRepository.findByEmailRequisitorAndDataLessThanOrderByDataDesc(email,
                                                hoje);
                        case "15dias":
                        default:
                                return reservaRepository.findByEmailRequisitorAndDataBetweenOrderByDataAsc(email, hoje,
                                                hoje.plusDays(15));
                }
        }

        public String determinarPeriodoParaData(LocalDate dataReserva) {
                LocalDate hoje = LocalDate.now();
                if (dataReserva.isBefore(hoje)) {
                        return "anteriores";
                }
                long diasDeDiferenca = ChronoUnit.DAYS.between(hoje, dataReserva);
                if (diasDeDiferenca <= 15) {
                        return "15dias";
                }
                if (diasDeDiferenca <= 30) {
                        return "30dias";
                }
                return "proximas";
        }

        public List<Reserva> listarTodasPorPeriodo(String periodo) {
                LocalDate hoje = LocalDate.now();

                switch (periodo) {
                        case "30dias":
                                return reservaRepository.findByDataBetweenOrderByDataAsc(hoje, hoje.plusDays(30));
                        case "proximas":
                                return reservaRepository.findByDataGreaterThanEqualOrderByDataAsc(hoje);
                        case "anteriores":
                                return reservaRepository.findByDataLessThanOrderByDataDesc(hoje);
                        case "15dias":
                        default:
                                return reservaRepository.findByDataBetweenOrderByDataAsc(hoje, hoje.plusDays(15));
                }
        }

        public void rejeitarReservasAuditorioPorData(LocalDate data) {
                // Busca todas as reservas (APROVADA, PENDENTE, etc.) para o Auditório no dia
                List<Reserva> reservasDoDia = reservaRepository.findByNumeroAndData("Auditorio", data);

                if (reservasDoDia != null && !reservasDoDia.isEmpty()) {
                        for (Reserva reserva : reservasDoDia) {
                                // Altera o status apenas se não estiver já rejeitada
                                if (reserva.getStatus() != StatusReserva.REJEITADA) {
                                        reserva.setStatus(StatusReserva.REJEITADA);
                                }
                        }
                        // Salva todas as alterações no banco de dados de uma vez
                        reservaRepository.saveAll(reservasDoDia);
                }
        }
}