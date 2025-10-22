package com.bd.sitebd.model.dto;

import com.bd.sitebd.model.Reserva;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;

// Esta classe formata os dados da Reserva para o JavaScript
public class ReservaDTO {

    public Long id;
    public String nome;
    public String evento;
    public String emailRequisitor;
    public LocalTime hora;
    public LocalTime horaFim;
    public String status;
    public boolean owner; // A propriedade que o JavaScript precisa!

    public ReservaDTO(Reserva reserva) {
        this.id = reserva.getId();
        this.nome = reserva.getNome();
        this.evento = reserva.getEvento();
        this.emailRequisitor = reserva.getEmailRequisitor();
        this.hora = reserva.getHora();
        this.horaFim = reserva.getHoraFim();
        this.status = reserva.getStatus().name();

        // Verifica se o email da reserva é o mesmo do usuário logado
        String usuarioLogadoEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        this.owner = usuarioLogadoEmail.equals(reserva.getEmailRequisitor());
    }
}