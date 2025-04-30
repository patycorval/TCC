package com.bd.sitebd.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @GetMapping("/contato") 
    public String Contato(Model model){
        model.addAttribute("activePage", "contato");
        return "contato"; 
    }

}