package com.bd.sitebd.controller;

import com.bd.sitebd.model.Usuario;
import com.bd.sitebd.model.dto.DiaCalendario;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.service.UsuarioService;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Tela de cadastro de usuários (apenas ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cadastro")
    public String exibirCadastro() {
        return "cadastro";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastro")
    public String processarCadastro(@RequestParam String email,
                                    @RequestParam String senha,
                                    @RequestParam String confirmarSenha, 
                                    @RequestParam TipoUsuario tipo,
                                    RedirectAttributes redirectAttributes) { 

        if (!senha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não conferem!");
            return "redirect:/admin/cadastro"; 
        }

        try {
            Usuario novo = new Usuario();
            novo.setEmail(email);
            novo.setSenha(passwordEncoder.encode(senha));
            novo.setTipo(tipo);

            usuarioService.salvar(novo);

            redirectAttributes.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/admin/cadastro";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auditorio-admin")
    public String auditorioAdmin(Model model) {
        YearMonth yearMonth = YearMonth.now();
        int diasNoMes = yearMonth.lengthOfMonth();

        // Lista para simular os dias do calendário
        List<DiaCalendario> diasDoMes = new ArrayList<>();

        // Lógica para simular o status de cada dia
        for (int i = 1; i <= diasNoMes; i++) {
            String status = "disponivel"; // Status padrão: disponível

            // Simulação de eventos em dias específicos
            if (i == 5 || i == 12 || i == 20) {
                status = "evento"; // Marquei os dias 5, 12 e 20 como evento
            } else if (i == 8 || i == 15) {
                status = "indisponivel"; // Marquei os dias 8 e 15 como indisponíveis
            }

            diasDoMes.add(new DiaCalendario(i, status));
        }

        model.addAttribute("diasDoMes", diasDoMes);
        model.addAttribute("activePage", "auditorio");
        return "auditorio-admin";
    }
}
