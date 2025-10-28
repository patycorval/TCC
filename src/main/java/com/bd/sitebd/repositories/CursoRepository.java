
package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findBySiglaAndPeriodo(String sigla, String periodo);
}