package com.bd.sitebd.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

public class Reserva {
    
    private int id;
    private String numero, nome;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data ;

    // @DateTimeFormat: Diz ao Spring que a variável data deve ser tratada com um formato específico de data. Garante que o Spring saiba como fazer a conversão entre o formato da data enviada e o tipo Java(LocalDate)
    // pattern = "yyyy-MM-dd": Especifica o padrão que o Spring deve usar para entender a data no formato YYYY-MM-DD. Este é o formato que o HTML <input type="date"> envia por padrão.

    private LocalTime hora; 
    private int duracao;
   

    public Reserva(){ 
        
    }

    public Reserva(int id, String numero, String nome, LocalDate data , LocalTime hora, int duracao){
        this.id=id;
        this.numero=numero;
        this.nome=nome;
        this.data=data;
        this.hora=hora;
        this.duracao=duracao;
    }

    public Reserva(String numero, String nome, LocalDate data , LocalTime hora, int duracao){
        this.numero=numero;
        this.nome=nome;
        this.data=data;
        this.hora=hora;
        this.duracao=duracao;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
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

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    
}