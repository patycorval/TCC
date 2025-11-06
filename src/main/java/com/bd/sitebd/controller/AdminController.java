package com.bd.sitebd.controller;

import com.bd.sitebd.model.Curso;
import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.Usuario;
import com.bd.sitebd.model.dto.DiaCalendario;
import com.bd.sitebd.model.enums.StatusReserva;
import org.springframework.security.core.Authentication;
import com.bd.sitebd.model.enums.TipoUsuario;
import com.bd.sitebd.repositories.CursoRepository;
import com.bd.sitebd.service.DiaBloqueadoService;
import com.bd.sitebd.service.ReservaService;
import com.bd.sitebd.service.UsuarioService;

// --- IMPORTS ADICIONADOS PARA CADASTRO DE SALA ---
import com.bd.sitebd.model.Sala;
import com.bd.sitebd.model.enums.Recurso;
import com.bd.sitebd.model.enums.TipoSala;
import com.bd.sitebd.service.SalaService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
// --- FIM DOS IMPORTS ADICIONADOS ---

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DiaBloqueadoService diaBloqueadoService;

    // --- INJEÇÃO ADICIONADA ---
    @Autowired
    private SalaService salaService;
    // --- FIM ---

    // --- SEU MÉTODO GET/cadastro (JÁ ESTÁ CORRETO) ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cadastro")
    public String exibirCadastro(Model model) {
        model.addAttribute("activePage", "cadastro"); // Mantido
        List<Curso> todosOsCursos = cursoRepository.findAll();
        model.addAttribute("listaDeCursos", todosOsCursos);

        System.out.println("--- DEBUG AdminController ---");
        System.out.println("Número de cursos encontrados no banco: " + todosOsCursos.size());
        if (!todosOsCursos.isEmpty()) {
            System.out.println("Primeiro curso: ID=" + todosOsCursos.get(0).getId() + ", Sigla="
                    + todosOsCursos.get(0).getSigla() + ", Periodo=" + todosOsCursos.get(0).getPeriodo());
        }
        System.out.println("Adicionando ao model com nome: listaDeCursos");

        return "cadastro";
    }

    // --- SEU MÉTODO POST/cadastro (JÁ ESTÁ CORRETO) ---
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastro")
    public String processarCadastro(@RequestParam String email,
            @RequestParam String nome,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            @RequestParam TipoUsuario tipo,
            @RequestParam(required = false) List<Long> cursos,
            RedirectAttributes redirectAttributes) {

        // ... (Seu código completo para processar o cadastro do usuário) ...
        // (Mantido exatamente como você enviou)
        if (!senha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não conferem!");
            redirectAttributes.addFlashAttribute("usuarioInput",
                    Map.of("email", email, "nome", nome, "tipo", tipo.name()));
            return "redirect:/admin/cadastro";
        }

        Set<Curso> cursosSelecionados = new HashSet<>();
        if ((tipo == TipoUsuario.PROFESSOR || tipo == TipoUsuario.MONITOR)) {
            if (cursos == null || cursos.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Professores e Monitores devem estar associados a pelo menos um curso!");
                redirectAttributes.addFlashAttribute("usuarioInput",
                        Map.of("email", email, "nome", nome, "tipo", tipo.name()));
                return "redirect:/admin/cadastro";
            }
            cursosSelecionados.addAll(cursoRepository.findAllById(cursos));
            if (cursosSelecionados.size() != cursos.size()) {
                System.err.println("Aviso: Alguns IDs de curso inválidos foram enviados ou não encontrados.");
            }
        }

        try {
            Usuario novo = new Usuario();
            novo.setEmail(email);
            novo.setNome(nome);
            novo.setSenha(passwordEncoder.encode(senha));
            novo.setTipo(tipo);
            novo.setCursos(cursosSelecionados);

            usuarioService.salvar(novo);

            redirectAttributes.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");

        } catch (Exception e) {
            String mensagemErro = "Erro ao cadastrar usuário.";
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint")
                    && e.getMessage().toLowerCase().contains("email")) {
                mensagemErro = "Este e-mail já está cadastrado.";
            } else {
                e.printStackTrace();
            }
            redirectAttributes.addFlashAttribute("mensagemErro", mensagemErro);
            redirectAttributes.addFlashAttribute("usuarioInput",
                    Map.of("email", email, "nome", nome, "tipo", tipo.name(), "cursos",
                            cursos != null ? cursos : List.of()));
        }
        return "redirect:/admin/cadastro";
    }

    // --- MÉTODOS ADICIONADOS PARA CADASTRO DE SALA ---

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/adicionar-sala")
    public String exibirFormularioSala(Model model) {
        model.addAttribute("activePage", "adicionar-sala");
        return "adicionar-sala";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/adicionar-sala")
    public String processarAdicionarSala(
            @RequestParam("numero") String numero,
            @RequestParam("localizacao") String localizacao,
            @RequestParam("capacidade") Integer capacidade,
            @RequestParam("qtdComputadores") int qtdComputadores,
            @RequestParam("tipo") TipoSala tipo, // Espera a String "SALA_AULA" ou "LABORATORIO"
            @RequestParam(name = "recursos", required = false) List<Recurso> recursos, // Espera Lista de Enums
            @RequestParam("imagem") MultipartFile imagem, // O arquivo de upload
            RedirectAttributes redirectAttributes) {

        // Validação básica do arquivo
        if (imagem.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A imagem é obrigatória.");
            return "redirect:/admin/adicionar-sala";
        }
        String contentType = imagem.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Formato de arquivo inválido. Apenas JPG ou PNG.");
            return "redirect:/admin/adicionar-sala";
        }

        try {
            Sala novaSala = new Sala();
            novaSala.setNumero(numero);
            novaSala.setLocalizacao(localizacao);
            novaSala.setCapacidade(capacidade);
            novaSala.setQtdComputadores(qtdComputadores);
            novaSala.setTipo(tipo);
            novaSala.setRecursos(recursos);
            novaSala.setAtiva(true);

            // Chama o SalaService para salvar o arquivo físico e atualizar a entidade
            salaService.salvarSalaComUpload(novaSala, imagem);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Sala '" + novaSala.getNumero() + "' cadastrada com sucesso!");
            return "redirect:/";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar a imagem no servidor.");
            return "redirect:/admin/adicionar-sala";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar a sala: " + e.getMessage());
            return "redirect:/admin/adicionar-sala";
        }
    }

    // --- SEUS MÉTODOS DE AUDITÓRIO (JÁ ESTÃO CORRETOS) ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auditorio-admin")
    public String auditorioAdmin(@RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            Model model) {

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String emailUsuario = authentication.getName();
        model.addAttribute("usuarioLogadoEmail", emailUsuario);

        YearMonth ym = (ano != null && mes != null) ? YearMonth.of(ano, mes) : YearMonth.now();
        List<LocalDate> diasBloqueados = diaBloqueadoService.buscarDiasBloqueadosNoMes(ym);
        YearMonth mesCorrente = YearMonth.now();
        model.addAttribute("desabilitarAnterior", !ym.isAfter(mesCorrente));
        List<Reserva> reservasAuditorio = reservaService.buscarReservasAuditorio(ym);

        List<DiaCalendario> diasDoMes = new ArrayList<>();
        LocalDate primeiroDiaDoMes = ym.atDay(1);
        int diaDaSemanaDoPrimeiroDia = primeiroDiaDoMes.getDayOfWeek().getValue();
        for (int i = 1; i < diaDaSemanaDoPrimeiroDia; i++) {
            diasDoMes.add(new DiaCalendario(0, "vazio"));
        }

        LocalDate hoje = LocalDate.now();

        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate dataDoDia = ym.atDay(i);
            DiaCalendario diaObj;

            if (dataDoDia.isBefore(hoje)) {
                diaObj = new DiaCalendario(i, "passado");
            } else {
                boolean foiBloqueadoPeloAdmin = diasBloqueados.contains(dataDoDia);
                boolean ehDomingo = dataDoDia.getDayOfWeek() == DayOfWeek.SUNDAY;

                if (foiBloqueadoPeloAdmin) {
                    diaObj = new DiaCalendario(i, "bloqueado");
                } else if (ehDomingo) {
                    diaObj = new DiaCalendario(i, "indisponivel");
                } else {
                    diaObj = new DiaCalendario(i, "disponivel");
                    List<Reserva> eventosDoDia = reservasAuditorio.stream()
                            .filter(r -> r.getData().isEqual(dataDoDia))
                            .sorted(Comparator.comparing(Reserva::getHora))
                            .toList();
                    diaObj.setEventos(eventosDoDia);
                }
            }
            diasDoMes.add(diaObj);
        }

        model.addAttribute("diasDoMes", diasDoMes);
        model.addAttribute("ym", ym);
        model.addAttribute("proximoMes", ym.plusMonths(1).getMonthValue());
        model.addAttribute("proximoAno", ym.plusMonths(1).getYear());
        model.addAttribute("anteriorMes", ym.minusMonths(1).getMonthValue());
        model.addAttribute("anteriorAno", ym.minusMonths(1).getYear());
        model.addAttribute("activePage", "auditorio");

        return "auditorio-admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/solicitacoes")
    public String exibirSolicitacoes(Model model) {
        model.addAttribute("solicitacoes", reservaService.buscarPorStatus(StatusReserva.PENDENTE));
        model.addAttribute("activePage", "solicitacoes");
        return "solicitacoes-admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/solicitacoes/atualizar")
    public String atualizarSolicitacao(@RequestParam("id") Long id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            StatusReserva novoStatus = StatusReserva.valueOf(status.toUpperCase());
            reservaService.atualizarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status da reserva atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Status inválido fornecido.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar o status da reserva.");
            e.printStackTrace();
        }
        return "redirect:/admin/solicitacoes";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorio/bloquear-dias")
    public String bloquearDias(
            @RequestParam(value = "diasSelecionados", required = false) Set<LocalDate> diasParaBloquear,
            RedirectAttributes redirectAttributes) {
        if (diasParaBloquear == null || diasParaBloquear.isEmpty()) {
            return "redirect:/admin/auditorio-admin";
        }
        try {
            diasParaBloquear.forEach(dia -> {
                diaBloqueadoService.bloquearDia(dia);
                reservaService.rejeitarReservasAuditorioPorData(dia); // Rejeita reservas ao bloquear
            });
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Dias bloqueados! Todas as reservas existentes nesses dias foram rejeitadas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao bloquear os dias.");
            e.printStackTrace();
        }
        YearMonth ym = YearMonth.from(diasParaBloquear.iterator().next());
        return "redirect:/admin/auditorio-admin?mes=" + ym.getMonthValue() + "&ano=" + ym.getYear();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorio/desbloquear-dias")
    public String desbloquearDias(
            @RequestParam(value = "diasSelecionados", required = false) Set<LocalDate> diasParaDesbloquear,
            RedirectAttributes redirectAttributes) {
        if (diasParaDesbloquear == null || diasParaDesbloquear.isEmpty()) {
            return "redirect:/admin/auditorio-admin";
        }
        try {
            diasParaDesbloquear.forEach(diaBloqueadoService::desbloquearDia);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Ocorreu um erro ao desbloquear os dias.");
            e.printStackTrace();
        }
        YearMonth ym = YearMonth.from(diasParaDesbloquear.iterator().next());
        return "redirect:/admin/auditorio-admin?mes=" + ym.getMonthValue() + "&ano=" + ym.getYear();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditorio/atualizar-status-massa")
    @ResponseBody
    public ResponseEntity<Void> atualizarStatusReservaEmMassa(@RequestBody List<UpdateStatusRequest> requests) {
        try {
            for (UpdateStatusRequest request : requests) {
                reservaService.atualizarStatus(request.getReservaId(), request.getNovoStatus());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public static class UpdateStatusRequest {
        private Long reservaId;
        private StatusReserva novoStatus;

        // Getters e Setters
        public Long getReservaId() {
            return reservaId;
        }

        public void setReservaId(Long reservaId) {
            this.reservaId = reservaId;
        }

        public StatusReserva getNovoStatus() {
            return novoStatus;
        }

        public void setNovoStatus(StatusReserva novoStatus) {
            this.novoStatus = novoStatus;
        }
    }
}