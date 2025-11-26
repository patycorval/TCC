package com.bd.sitebd.service;

import com.bd.sitebd.model.DiaBloqueado;
import com.bd.sitebd.repositories.DiaBloqueadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiaBloqueadoService {

    @Autowired
    private DiaBloqueadoRepository diaBloqueadoRepository;

    @Transactional
    public void bloquearDia(LocalDate data) {
        if (diaBloqueadoRepository.findByData(data) == null) {
            diaBloqueadoRepository.save(new DiaBloqueado(data));
        }
    }

    @Transactional
    public void desbloquearDia(LocalDate data) {
        diaBloqueadoRepository.deleteByData(data); 
    }

    public List<LocalDate> buscarDiasBloqueadosNoMes(YearMonth ym) {
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();
        return diaBloqueadoRepository.findByDataBetween(inicio, fim)
                .stream()
                .map(DiaBloqueado::getData)
                .collect(Collectors.toList());
    }
}