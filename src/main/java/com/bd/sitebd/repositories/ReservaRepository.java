package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
        List<Reserva> findByNumeroAndData(String numero, LocalDate data);

        List<Reserva> findByStatus(StatusReserva status);

        List<Reserva> findByNumeroAndDataBetween(String numero, LocalDate dataInicio, LocalDate dataFim);

        List<Reserva> findByEmailRequisitor(String emailRequisitor);

        List<Reserva> findByNumeroAndStatusAndDataBetweenOrderByDataAscHoraAsc(String numero, StatusReserva status,
                        LocalDate dataInicio, LocalDate dataFim);

        // NOVO MÉTODO
        @Query("SELECT r FROM Reserva r WHERE r.numero = :numero AND r.data BETWEEN :dataInicio AND :dataFim " +
                        "AND (r.status = com.bd.sitebd.model.enums.StatusReserva.APROVADA OR r.emailRequisitor = :emailRequisitor)")
        List<Reserva> findReservasAuditorioParaUsuario(
                        @Param("numero") String numero,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim,
                        @Param("emailRequisitor") String emailRequisitor);
}