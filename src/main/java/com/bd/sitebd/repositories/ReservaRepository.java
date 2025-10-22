package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
        List<Reserva> findByNumeroAndData(String numero, LocalDate data);

        List<Reserva> findByStatus(StatusReserva status);

        List<Reserva> findByEmailRequisitor(String emailRequisitor);

        List<Reserva> findByNumeroAndStatusAndDataBetweenOrderByDataAscHoraAsc(String numero, StatusReserva status,
                        LocalDate dataInicio, LocalDate dataFim);

        // Novo método adicionado
        List<Reserva> findByNumeroAndStatusAndDataBetweenAndEmailRequisitorOrderByDataAscHoraAsc(String numero,
                        StatusReserva status,
                        LocalDate dataInicio, LocalDate dataFim, String emailRequisitor);

}