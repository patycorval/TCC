package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Necessário para a consulta customizada
import org.springframework.data.repository.query.Param; // Necessário para a consulta customizada

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

        List<Reserva> findByNumeroAndData(String numero, LocalDate data);

        List<Reserva> findByStatus(StatusReserva status);

        List<Reserva> findByEmailRequisitor(String emailRequisitor);

        List<Reserva> findByNumeroAndStatusAndDataBetweenOrderByDataAscHoraAsc(String numero, StatusReserva status,
                        LocalDate dataInicio, LocalDate dataFim);

        List<Reserva> findByNumeroAndStatusAndDataBetweenAndEmailRequisitorOrderByDataAscHoraAsc(String numero,
                        StatusReserva status, LocalDate dataInicio, LocalDate dataFim, String emailRequisitor);

        List<Reserva> findByCursoIdAndSemestreAndDataBetweenOrderByDataAscHoraAsc(
                        Long cursoId, Integer semestre, LocalDate dataInicio, LocalDate dataFim);

        // temConflitoComAprovadas no ReservaService
        List<Reserva> findByNumeroAndDataAndStatus(String numero, LocalDate data, StatusReserva status);

        List<Reserva> findByNumeroAndDataBetweenAndStatusNotOrderByDataAscHoraAsc(
                        String numero,
                        LocalDate dataInicio,
                        LocalDate dataFim,
                        StatusReserva status);

        // buscarReservasAuditorioParaUsuario no ReservaService
        @Query("SELECT r FROM Reserva r WHERE r.numero = :numero " +
                        "AND r.data BETWEEN :dataInicio AND :dataFim " +
                        "AND (r.status = 'APROVADA' OR r.emailRequisitor = :email) " +
                        "ORDER BY r.data ASC, r.hora ASC")
        List<Reserva> findReservasAuditorioParaUsuario(
                        @Param("numero") String numero,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim,
                        @Param("email") String email);

        List<Reserva> findByEmailRequisitorAndDataBetweenOrderByDataAsc(String emailRequisitor, LocalDate inicio,
                        LocalDate fim);

        List<Reserva> findByEmailRequisitorAndDataGreaterThanEqualOrderByDataAsc(String emailRequisitor,
                        LocalDate data);

        List<Reserva> findByEmailRequisitorAndDataLessThanOrderByDataDesc(String emailRequisitor, LocalDate data);

        List<Reserva> findByDataBetweenOrderByDataAsc(LocalDate dataInicio, LocalDate dataFim);

        List<Reserva> findByDataGreaterThanEqualOrderByDataAsc(LocalDate data);

        List<Reserva> findByDataLessThanOrderByDataDesc(LocalDate data);

}