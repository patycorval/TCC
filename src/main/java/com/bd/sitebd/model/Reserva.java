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

    private LocalTime hora;
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    // --- NOVOS CAMPOS ---
    @ManyToOne(fetch = FetchType.LAZY) // Lazy para não carregar sempre
    @JoinColumn(name = "curso_id")
    private Curso curso;

    private Integer semestre; // Usamos Integer para permitir null
    // --- FIM NOVOS CAMPOS ---

    // Construtores
    public Reserva() {
    }

    // Getters e Setters (EXISTENTES)
    // ... (id, numero, nome, evento, emailRequisitor, data, hora, horaFim, status)
    // ...
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

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    @Transient
    private boolean isOwner;

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}