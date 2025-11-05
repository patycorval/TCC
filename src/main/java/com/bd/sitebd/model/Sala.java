package com.bd.sitebd.model;

import jakarta.persistence.*;
import java.util.List;
import com.bd.sitebd.model.enums.Recurso;
import com.bd.sitebd.model.enums.TipoSala;
import java.util.stream.Collectors;

@Entity
@Table(name = "sala") // Garante que o nome da tabela esteja correto
public class Sala {

    // --- SEUS CAMPOS EXISTENTES ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int qtdComputadores;
    private String numero;
    private Integer capacidade;
    private String localizacao;

    @Enumerated(EnumType.STRING) // Assumindo que seu 'tipo' é um Enum
    private TipoSala tipo;

    private boolean ativa = true;

    @ElementCollection(targetClass = Recurso.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "sala_recursos", joinColumns = @JoinColumn(name = "sala_id"))
    @Column(name = "recurso")
    private List<Recurso> recursos;

    // --- CAMPO DE IMAGEM ATUALIZADO ---
    @Column(name = "imagem_url") // Mapeia para a nova coluna do banco
    private String imagemUrl; // Apenas um campo de texto
    // --- FIM DA ATUALIZAÇÃO ---

    // Construtores
    public Sala() {
    }
    // (Seu outro construtor, se houver)

    // --- GETTERS E SETTERS ---

    // (Getters/setters existentes: id, numero, capacidade, etc.)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQtdComputadores() {
        return qtdComputadores;
    }

    public void setQtdComputadores(int qtdComputadores) {
        this.qtdComputadores = qtdComputadores;
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

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
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

    // --- GETTER E SETTER PARA IMAGEM_URL ---
    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
    // --- FIM ---

    // --- Seus métodos @Transient (MANTENHA-OS) ---
    @Transient
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
        return this.recursos.stream()
                .map(Recurso::name)
                .collect(Collectors.joining(","));
    }

    @Transient
    public String getTipoSalaDisplayName() {
        return this.getTipoSala() == TipoSala.LABORATORIO ? "Laboratório" : "Sala de Aula";
    }
}