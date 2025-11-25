package com.bd.sitebd.model.dto;

import com.bd.sitebd.model.Reserva;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiaCalendario {

    private int dia;
    private String status;
    private List<Reserva> eventos = new ArrayList<>();
    private boolean bloqueado;

    public DiaCalendario(int dia, String status) {
        this.dia = dia;
        this.status = status;
    }

    public DiaCalendario(int dia, String status, List<Reserva> eventos) {
        this.dia = dia;
        this.status = status;
        this.eventos = eventos;
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

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public String getEventosAsJson() {
        if (this.eventos == null || this.eventos.isEmpty()) {
            return "[]";
        }

        List<ReservaJsonDTO> eventosDto = this.eventos.stream()
                .map(ReservaJsonDTO::new)
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {

            return mapper.writeValueAsString(eventosDto);
        } catch (JsonProcessingException e) {

            return "[]";
        }
    }
}