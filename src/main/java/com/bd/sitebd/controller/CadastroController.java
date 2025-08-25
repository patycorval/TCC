package com.bd.sitebd.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.ReservaService;
import com.bd.sitebd.model.Tool;
//gerencia as rotas HTTP e interage com modelo e as views
import com.bd.sitebd.model.dto.DiaCalendario;

@Controller
public class CadastroController {
    @Autowired //injetar dependências 
    private ApplicationContext context;

    @GetMapping("/") 
    public String Principal(Model model){ //"caixa" onde coloco os dados q vao p front
        model.addAttribute("activePage", "principal");
        return "principal"; 
    }

    @GetMapping("/atualizar/{id}") //obter
    public String atualizar(Model model, @PathVariable int id) { // id será passado como argumento para o método
        ReservaService cs = context.getBean(ReservaService.class); //O Spring automaticamente injeta uma instância de ReservaService
        Reserva res = cs.obterReserva(id); // metodo do ReservaService
        model.addAttribute("id", id);
        model.addAttribute("reserva", res);
        return "atualizar"; 
    }


    @PostMapping("/atualizar/{id}") //enviar
    public String atualizar(@PathVariable int id, @ModelAttribute Reserva res){// @ModelAttribute instrui o Spring a vincular os dados do forms ao objeto Reserva.
        ReservaService cs = context.getBean(ReservaService.class);
        cs.atualizarReserva(id, res); 
        return "redirect:/listagem";
    }


    @GetMapping("/reservar")
    public String reserva(@RequestParam("numero") String numero, Model model) {
        // Criando o objeto Reserva com o número da sala e passando para o modelo
        model.addAttribute("reserva", new Reserva(numero, "", null, null, 0));
        model.addAttribute("activePage", "reservar");
        return "reservar";
}

    @PostMapping("/reservar")
    public String cadastrar(@ModelAttribute Reserva res) {
        ReservaService cs = context.getBean(ReservaService.class);
        cs.inserir(res);
        return "sucesso"; 
    }

// { "id": 1, "numero": "101", "data": "2024-11-23", "duracao": 2 },
    @GetMapping("/listagem")
    public String listagem(Model model){ //adc dados a pag view
        ReservaService cs = context.getBean(ReservaService.class);
        List<Map<String,Object>> lista = cs.obterTodasReservas(); //retorna uma lista onde cada mapa contém dados de uma reserva, tendo a chave "id" e seu valor correspondente
        List<Reserva> listaReservas = new ArrayList<Reserva>(); // lista contendo objetos do tipo Reserva.
        for(Map<String,Object> registro : lista){ //percorre cada registro e vai adc no array de Reserva
            listaReservas.add(Tool.converterReserva(registro));
        }
        model.addAttribute("reservas", listaReservas); //adc a lista de objetos Reserva ao modelo com o nome "reservas", para que esses dados possam ser usados na view
        model.addAttribute("activePage", "listagem");
        return "listagem";
    }

    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable int id){
        ReservaService cs = context.getBean(ReservaService.class);
        cs.deletarReserva(id);
        return "redirect:/listagem";
    }

    // @GetMapping("/auditorio")
    // public String auditorio(Model model) {
    //     YearMonth yearMonth = YearMonth.now();
    //     int diasNoMes = yearMonth.lengthOfMonth();

    //     ReservaService rs = context.getBean(ReservaService.class);
    //     List<Map<String, Object>> reservasMap = rs.obterTodasReservas();
    //     List<Reserva> reservas = new ArrayList<>();
    //     for (Map<String, Object> registro : reservasMap) {
    //         reservas.add(Tool.converterReserva(registro));
    //     }

    //     List<DiaCalendario> diasDoMes = new ArrayList<>();
    //     LocalDate hoje = LocalDate.now();

    //     for (int i = 1; i <= diasNoMes; i++) {
    //         final int diaAtual = i;
    //         boolean temReserva = reservas.stream()
    //             .anyMatch(reserva -> {
    //                 // Verifique se a data da reserva não é nula antes de tentar acessá-la
    //                 return reserva.getData() != null && reserva.getData().getDayOfMonth() == diaAtual;
    //             });
            
    //         // Lógica para definir os status
    //         String status;
    //         if (temReserva) {
    //             status = "evento";
    //         } else if (LocalDate.of(hoje.getYear(), hoje.getMonth(), i).isBefore(hoje)) {
    //             // Se a data já passou, considere indisponível (opcional, pode ajustar a regra)
    //             status = "indisponivel";
    //         } else {
    //             status = "disponivel";
    //         }

    //         diasDoMes.add(new DiaCalendario(diaAtual, status));
    //     }

    //     model.addAttribute("diasDoMes", diasDoMes);
    //     model.addAttribute("activePage", "auditorio");
    //     return "auditorio";
    // }

    @GetMapping("/auditorio")
public String auditorio(Model model) {
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
    return "auditorio";
}

    @GetMapping("/login")
    public String login(Model model) {
    model.addAttribute("activePage", "login");
    return "login";
    }
}