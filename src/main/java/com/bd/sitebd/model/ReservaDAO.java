package com.bd.sitebd.model;

// import java.time.LocalDate;
// import java.time.LocalTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
//utiliza JDBC template p interagir c o BD

@Repository
public class ReservaDAO {
    /* DAO = Data Acssess Object */

    @Autowired
    DataSource dataSource; // injetar uma instância de DataSource

    JdbcTemplate jdbc; // simplifica a interação com o banco de dados,

    @PostConstruct
    private void initialize() {
        jdbc = new JdbcTemplate(dataSource); // jdbc configurada com o DataSource, executa operações no bd de maneira
                                             // mais simples com JdbcTemplate.
    }

    public void inserir(Reserva res) {
        String sql = "INSERT INTO reserva(numero,nome,data, hora, duracao) VALUES (?,?,?,?,?);";
        Object[] parametros = new Object[5];
        parametros[0] = res.getNumero();
        parametros[1] = res.getNome();
        parametros[2] = res.getData();
        parametros[3] = res.getHora();
        parametros[4] = res.getDuracao();
        jdbc.update(sql, parametros);
    }

    // [ {id: 1, nome: teste1, cpf: 123456789-00}
    // , {id: 2, nome: teste2, cpf: 323456789-00}
    // ]
    public List<Map<String, Object>> obterTodasReservas() {
        String sql = "Select * from reserva;";
        return jdbc.queryForList(sql);
    }

    public void atualizarCliente(int id, Reserva res) {
        String sql = "UPDATE reserva SET ";
        sql += "numero = ?, nome = ?, data = ?, hora = ?, duracao = ? WHERE id = ?";
        jdbc.update(sql, res.getNumero(), res.getNome(), res.getData(), res.getHora(), res.getDuracao(), id);
    }

    public Reserva obterReserva(int id) {
        String sql = "Select * from reserva where id = ?";
        return Tool.converterReserva(jdbc.queryForMap(sql, id));
    }

    public void deletarReserva(int id) {
        String sql = "DELETE FROM reserva where id = ?";
        jdbc.update(sql, id);
    }
}
