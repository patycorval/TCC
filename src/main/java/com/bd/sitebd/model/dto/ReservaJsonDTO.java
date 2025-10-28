package com.bd.sitebd.model.dto;

import com.bd.sitebd.model.Reserva;
import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) para serializar os dados da Reserva para o
 * JavaScript.
 * Inclui apenas os campos necessários para o frontend.
 */
public class ReservaJsonDTO {

    private Long id;
    private String evento;
    private LocalTime hora;
    private LocalTime horaFim;
    private String emailRequisitor;
    private String periodoIdeal;
    private String nome;
    private String status;

    public ReservaJsonDTO(Reserva reserva) {
        this.id = reserva.getId();
        this.evento = reserva.getEvento();
        this.hora = reserva.getHora();
        this.horaFim = reserva.getHoraFim();
        this.emailRequisitor = reserva.getEmailRequisitor();
        this.periodoIdeal = reserva.getPeriodoIdeal();
        this.nome = reserva.getNome();
        this.status = reserva.getStatus().name();
    }

    // Getters públicos para que a biblioteca Jackson possa encontrá-los e criar o
    // JSON
    public Long getId() {
        return id;
    }

    public String getEvento() {
        return evento;
    }

    public LocalTime getHora() {
        return hora;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public String getEmailRequisitor() {
        return emailRequisitor;
    }

    public String getPeriodoIdeal() {
        return periodoIdeal;
    }

    public String getNome() {
        return nome;
    }

    public String getStatus() {
        return status;
    }
}