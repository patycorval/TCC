package com.bd.sitebd.config;

import com.bd.sitebd.model.Usuario;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.annotation.PostConstruct;

@Configuration
public class AdminInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Checa se o admin já existe
        if (usuarioRepository.findByEmail("admin@gmail.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@gmail.com");
            admin.setSenha(passwordEncoder.encode("123")); // criptografa a senha
            admin.setTipo(TipoUsuario.ADMIN);

            usuarioRepository.save(admin);
            System.out.println("Admin criado: admin@gmail.com / 123");
        }
    }
}
