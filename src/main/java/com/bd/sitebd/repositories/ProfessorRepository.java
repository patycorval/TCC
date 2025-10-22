package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    List<Professor> findByCursos_Id(Long cursoId);
}