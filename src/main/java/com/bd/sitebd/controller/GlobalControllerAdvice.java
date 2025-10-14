package com.bd.sitebd.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addUserDetailsToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            // Pega a primeira "ROLE" do usuário e a formata para exibição
            String tipo = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("")
                    .replace("ROLE_", "");

            model.addAttribute("usuarioEmail", email);
            model.addAttribute("usuarioTipo", tipo);
        }
    }
}