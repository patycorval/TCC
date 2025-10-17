package com.bd.sitebd.model;

import jakarta.persistence.*;
import java.util.List;

import com.bd.sitebd.model.enums.Recurso;
import com.bd.sitebd.model.enums.TipoSala;
import java.util.stream.Collectors;

@Entity
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int qtdComputadores;
    private String numero; // NUMERO DA SALA
    private Integer capacidade; // QT ALUNO
    private String localizacao; // ANDAR
    private TipoSala tipo; // Ex: Laboratório, Sala comum
    private boolean ativa = true; // SE ESTA RESERVADA OU NAO

    @ElementCollection(targetClass = Recurso.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "sala_recursos", joinColumns = @JoinColumn(name = "sala_id"))
    @Column(name = "recurso")
    private List<Recurso> recursos;

    public Sala() {
    }

    public Sala(String numero, Integer capacidade, String localizacao, TipoSala tipo, boolean ativa,
            List<Recurso> recursos, int qtdComputadores) {
        this.numero = numero;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.tipo = tipo;
        this.ativa = ativa;
        this.recursos = recursos;
        this.qtdComputadores = qtdComputadores;
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

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
    }

    public int getQtdComputadores() {
        return qtdComputadores;
    }

    @Transient // transient nao existe no banco
    public TipoSala getTipoSala() {
        if (this.qtdComputadores > 1) {
            return TipoSala.LABORATORIO;
        }
        return TipoSala.SALA_AULA;
    }

    @Transient
    public String getRecursosAsString() {
        if (this.recursos == null || this.recursos.isEmpty()) {
            return "";
        }
        // Para cada recurso, pega seu nome (ex: "TELEVISOR")
        // Junta todos os nomes em uma String, separados por ","
        return this.recursos.stream()
                            .map(Recurso::name)
                            .collect(Collectors.joining(","));
    }

    @Transient
    public String getTipoSalaDisplayName() {
    return this.getTipoSala() == TipoSala.LABORATORIO ? "Laboratório" : "Sala de Aula";
    }
}
