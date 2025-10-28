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

    // Construtores
    public DiaCalendario(int dia, String status) {
        this.dia = dia;
        this.status = status;
    }
     public DiaCalendario(int dia, String status, List<Reserva> eventos) {
        this.dia = dia;
        this.status = status;
        this.eventos = eventos;
    }

    // Getters e Setters
    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Reserva> getEventos() { return eventos; }
    public void setEventos(List<Reserva> eventos) { this.eventos = eventos; }
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    /**
     * MÉTODO ATUALIZADO
     * Converte a lista de Reservas para um JSON string, garantindo que o campo 'periodoIdeal' seja incluído.
     */
    public String getEventosAsJson() {
        if (this.eventos == null || this.eventos.isEmpty()) {
            return "[]";
        }

        // 1. Mapeia a lista de Reserva para uma lista do nosso DTO
        List<ReservaJsonDTO> eventosDto = this.eventos.stream()
                .map(ReservaJsonDTO::new)
                .collect(Collectors.toList());

        // 2. Usa a biblioteca Jackson (já incluída no Spring) para criar o JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Essencial para formatar datas/horas como LocalTime
        try {
            // 3. Retorna a string JSON que agora contém 'periodoIdeal'
            return mapper.writeValueAsString(eventosDto);
        } catch (JsonProcessingException e) {
            // Em caso de erro, retorna um array vazio para não quebrar o JavaScript
            return "[]";
        }
    }
}