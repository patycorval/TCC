package com.bd.sitebd.model.dto;

import com.bd.sitebd.model.Reserva;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;

public class DiaCalendario {

    private int dia;
    private String status;
    private List<Reserva> eventos;

    public DiaCalendario(int dia, String status) {
        this.dia = dia;
        this.status = status;
        this.eventos = new ArrayList<>(); // Inicializa a lista
    }

    // Getters e Setters
    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Reserva> getEventos() {
        return eventos;
    }

    public void setEventos(List<Reserva> eventos) {
        this.eventos = eventos;
    }

    /**
     * Este método converte a lista de reservas para uma string JSON.
     * O Thymeleaf usará este método para popular o atributo 'data-eventos' no HTML.
     */
    public String getEventosAsJson() {
        if (eventos == null || eventos.isEmpty()) {
            return "[]";
        }
        // Converte a lista de Reserva para uma lista de ReservaDTO
        List<ReservaDTO> eventosDto = eventos.stream().map(ReservaDTO::new).toList();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Módulo para formatar datas e horas corretamente
        try {
            return mapper.writeValueAsString(eventosDto);
        } catch (JsonProcessingException e) {
            return "[]"; // Retorna uma lista vazia em caso de erro
        }
    }
}