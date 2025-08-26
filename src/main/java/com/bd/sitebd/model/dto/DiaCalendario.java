package com.bd.sitebd.model.dto;

public class DiaCalendario {
    private int dia;
    private String status;

    public DiaCalendario(int dia, String status) {
        this.dia = dia;
        this.status = status;
    }

    // Getters and Setters
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
}
