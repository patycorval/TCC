package com.bd.sitebd.model;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// responsável pela lógica de negócios
@Service
public class ReservaService {
    // CONEXAO É O SERVICE, OU SEJA, O FRONT SO ENXERGA AQUI, ENQUANTO O DAO É ESCONDIDO (A VIEW NAO O VÊ), chama dao
    
    @Autowired
    ReservaDAO rdao;// injeta DAO

    public void inserir(Reserva res){
        rdao.inserir(res); // passa o obj pelo metodo do dao p inserir no bd
    }

    public List<Map<String,Object>> obterTodasReservas(){ //retorna uma lista de mapas 
        return rdao.obterTodasReservas();
    }

    public void atualizarReserva(int id, Reserva res){
        rdao.atualizarCliente(id, res);
    }

    public Reserva obterReserva(int id){
        return rdao.obterReserva(id);
    }

    public void deletarReserva(int id){
        rdao.deletarReserva(id);
    }
}
