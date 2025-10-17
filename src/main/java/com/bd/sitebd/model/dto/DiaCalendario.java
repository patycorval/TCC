package com.bd.sitebd.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.bd.sitebd.model.Reserva;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DiaCalendario {

    private int dia;
    private String status;
    private List<Reserva> eventos = new ArrayList<>();

    public DiaCalendario(int dia, String status) {
        this.dia = dia;
        this.status = status;
    }

    // NOVO MÉTODO
    public String getEventosAsJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Essencial para formatar datas/horas
        try {
            return mapper.writeValueAsString(this.eventos);
        } catch (JsonProcessingException e) {
            return "[]"; 
        }
    }

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
}