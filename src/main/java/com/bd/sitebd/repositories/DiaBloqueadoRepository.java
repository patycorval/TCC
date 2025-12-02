package com.bd.sitebd.repositories;

import com.bd.sitebd.model.DiaBloqueado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaBloqueadoRepository extends JpaRepository<DiaBloqueado, Long> {

    List<DiaBloqueado> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    DiaBloqueado findByData(LocalDate data);

    void deleteByData(LocalDate data);
}