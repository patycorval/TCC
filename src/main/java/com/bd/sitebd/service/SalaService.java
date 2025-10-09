package com.bd.sitebd.service;

import com.bd.sitebd.model.Sala;
import com.bd.sitebd.repositories.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    public void apagarTodasSalas() {
        salaRepository.deleteAll();
    }

    public void salvarTodas(List<Sala> salas) {
        salaRepository.saveAll(salas);
    }

    public List<Sala> listarTodas() {
        return salaRepository.findAll();
    }

    public Sala buscarPorId(Long id) {
        Optional<Sala> optional = salaRepository.findById(id);
        return optional.orElse(null);
    }

    public Sala salvar(Sala sala) {
        return salaRepository.save(sala);
    }

    public void desativarSala(Long id) {
        Sala sala = buscarPorId(id);
        if (sala != null) {
            sala.setAtiva(false);
            salaRepository.save(sala);
        }
    }

    //  FILTRO  

    public List<Sala> getSalasFiltradas(String andar, String recurso, String tiposala) {
        // Pega TODAS as salas do banco
        List<Sala> todasAsSalas = salaRepository.findAll();

        // Aplica os filtros e retorna a lista resultante
        return todasAsSalas.stream()
                .filter(sala -> andar == null || andar.isEmpty() || (sala.getLocalizacao() != null && sala.getLocalizacao().startsWith(andar)))
                .filter(sala -> tiposala == null || tiposala.isEmpty() || sala.getTipoSala().name().equals(tiposala))
                .filter(sala -> recurso == null || recurso.isEmpty() || sala.getRecursosAsString().contains(recurso))
                .toList();
    }
}