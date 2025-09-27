package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Sala;
import com.bd.sitebd.model.enums.Recurso;
import com.bd.sitebd.model.enums.TipoSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

    // Personalizações úteis

    List<Sala> findByAtivaTrue(); // Listar apenas salas ativas

    List<Sala> findByTipo(TipoSala tipo); // Filtrar por tipo de sala

    List<Sala> findByRecursosContaining(Recurso recurso); // Buscar salas com recurso específico (ex: "PROJETOR")
}