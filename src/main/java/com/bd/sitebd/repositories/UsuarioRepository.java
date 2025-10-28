package com.bd.sitebd.repositories;

import com.bd.sitebd.model.Usuario;
import com.bd.sitebd.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByTipoIn(List<TipoUsuario> tipos);

    List<Usuario> findByCursos_IdAndTipoIn(Long cursoId, List<TipoUsuario> tipos);

    List<Usuario> findByCursos_Id(Long cursoId);
}