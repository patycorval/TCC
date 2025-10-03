package com.bd.sitebd.model;

import com.bd.sitebd.model.enums.StatusReserva;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String nome;
    private String evento;
    private String emailRequisitor;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    private LocalTime hora; // Nome do campo corrigido para 'hora'
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    // Construtores
    public Reserva() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getEmailRequisitor() {
        return emailRequisitor;
    }

    public void setEmailRequisitor(String emailRequisitor) {
        this.emailRequisitor = emailRequisitor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    // Getters e Setters corrigidos para o campo 'hora'
    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }
}