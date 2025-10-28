package com.bd.sitebd.service;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

        @Autowired
        private ReservaRepository reservaRepository;

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

                // 2. Pega os horários da nova solicitação
                LocalTime inicioNova = novaReserva.getHora();
                LocalTime fimNova = novaReserva.getHoraFim();

                // 3. Verifica se há algum conflito de horário
                for (Reserva r : reservasAprovadasNoDia) {
                        LocalTime inicioExistente = r.getHora();
                        LocalTime fimExistente = r.getHoraFim();
                        // Lógica de verificação de sobreposição
                        boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
                        if (conflito) {
                                return true; // Encontrou um conflito!
                        }
                }
                return false; // Nenhum conflito encontrado
        }

        public Reserva salvar(Reserva reserva) {
                if (reserva.getHoraFim().isBefore(reserva.getHora())
                                || reserva.getHoraFim().equals(reserva.getHora())) {
                        throw new IllegalArgumentException("A hora de fim não pode ser menor ou igual à de início.");
                }
                Duration duracao = Duration.between(reserva.getHora(), reserva.getHoraFim());
                if (duracao.toMinutes() < 30) {
                        throw new IllegalArgumentException("A reserva deve durar ao menos 30 minutos.");
                }
                if (!"Auditorio".equals(reserva.getNumero()) && temConflito(reserva)) {
                        throw new IllegalArgumentException("Já existe uma reserva para essa sala nesse horário.");
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
                if (reserva.getHoraFim().isBefore(reserva.getHora())
                                || reserva.getHoraFim().equals(reserva.getHora())) {
                        throw new IllegalArgumentException("A hora de fim não pode ser menor ou igual à de início.");
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

        // Este método busca apenas reservas APROVADAS (visão geral)
        public List<Reserva> buscarReservasAuditorio(YearMonth ym) {
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                return reservaRepository.findByNumeroAndStatusAndDataBetweenOrderByDataAscHoraAsc("Auditorio",
                                StatusReserva.APROVADA, startOfMonth, endOfMonth);
        }

        // Este método busca as reservas para a visão do usuário (aprovadas de outros +
        // todas as suas)
        public List<Reserva> buscarReservasAuditorioParaUsuario(YearMonth ym, String email) {
                LocalDate startOfMonth = ym.atDay(1);
                LocalDate endOfMonth = ym.atEndOfMonth();
                return reservaRepository.findReservasAuditorioParaUsuario("Auditorio", startOfMonth, endOfMonth, email);
        }

        // NOVO MÉTODO COM A LÓGICA DO FILTRO
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
}