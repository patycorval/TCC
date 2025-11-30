package com.bd.sitebd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class passwordgenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senha = "123"; // a senha que você quer criptografar
        String senhaCriptografada = encoder.encode(senha);
        System.out.println("Senha criptografada: " + senhaCriptografada);
    }
}
