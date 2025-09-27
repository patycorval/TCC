package com.bd.sitebd.service;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.enums.StatusReserva;
import com.bd.sitebd.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public boolean temConflito(Reserva novaReserva) {
        List<Reserva> reservasNoMesmoDia = reservaRepository.findByNumeroAndData(novaReserva.getNumero(),
                novaReserva.getData());

        LocalTime inicioNova = novaReserva.getHora();
        LocalTime fimNova = inicioNova.plusHours(novaReserva.getDuracao());

        for (Reserva r : reservasNoMesmoDia) {
            LocalTime inicioExistente = r.getHora();
            LocalTime fimExistente = inicioExistente.plusHours(r.getDuracao());

            boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
            if (conflito) {
                return true;
            }
        }
        return false;
    }

    public boolean temConflitoAtualizacao(Reserva novaReserva) {
        List<Reserva> reservasNoMesmoDia = reservaRepository.findByNumeroAndData(novaReserva.getNumero(),
                novaReserva.getData());

        LocalTime inicioNova = novaReserva.getHora();
        LocalTime fimNova = inicioNova.plusHours(novaReserva.getDuracao());

        for (Reserva r : reservasNoMesmoDia) {
            if (novaReserva.getId() != null && r.getId().equals(novaReserva.getId())) {
                continue;
            }

            LocalTime inicioExistente = r.getHora();
            LocalTime fimExistente = inicioExistente.plusHours(r.getDuracao());

            boolean conflito = inicioNova.isBefore(fimExistente) && fimNova.isAfter(inicioExistente);
            if (conflito) {
                return true;
            }
        }
        return false;
    }

    public Reserva salvar(Reserva reserva) {
        // Se a reserva não for de auditório E tiver conflito, lança a exceção
        if (!"Auditorio".equals(reserva.getNumero()) && temConflito(reserva)) {
            throw new IllegalArgumentException("Já existe uma reserva para essa sala nesse horário.");
        }

        // Define o status para reservas de sala
        if (!"Auditorio".equals(reserva.getNumero())) {
            reserva.setStatus(StatusReserva.APROVADA);
        }

        return reservaRepository.save(reserva);
    }

    public Reserva atualizar(Reserva reserva) {
        // Se a reserva não for de auditório E tiver conflito, lança a exceção
        if (!"Auditorio".equals(reserva.getNumero()) && temConflitoAtualizacao(reserva)) {
            throw new IllegalArgumentException("Já existe uma reserva para essa sala nesse horário.");
        }
        return reservaRepository.save(reserva);
    }

    public List<Reserva> buscarPorStatus(StatusReserva status) {
        return reservaRepository.findByStatus(status);
    }

    public void atualizarStatus(Long id, StatusReserva novoStatus) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        if (reservaOptional.isPresent()) {
            Reserva reserva = reservaOptional.get();
            reserva.setStatus(novoStatus);
            reservaRepository.save(reserva);
        }
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> listarPorUsuario(String email) {
        return reservaRepository.findByEmailRequisitor(email);
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public void deletar(Long id) {
        reservaRepository.deleteById(id);
    }
}