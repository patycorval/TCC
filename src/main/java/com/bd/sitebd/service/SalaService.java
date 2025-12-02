package com.bd.sitebd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.bd.sitebd.model.Reserva;
import com.bd.sitebd.model.Sala;
import com.bd.sitebd.repositories.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate; 
import java.time.LocalTime;
import java.util.ArrayList; 
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ReservaService reservaService;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

    public Sala salvarSalaComUpload(Sala sala, MultipartFile arquivoImagem) throws IOException {

        if (arquivoImagem.isEmpty()) {
            throw new IOException("Arquivo de imagem está vazio.");
        }

        String originalFilename = arquivoImagem.getOriginalFilename();
        String extensao = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extensao = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extensao;

        Path caminhoFisico = Paths.get(uploadDir, uniqueFilename);

        Files.createDirectories(caminhoFisico.getParent());
        Files.copy(arquivoImagem.getInputStream(), caminhoFisico);

        String urlPublica = "/uploads/" + uniqueFilename;
        sala.setImagemUrl(urlPublica);

        return salaRepository.save(sala);
    }


   public List<Sala> getSalasFiltradas(String andar, String recurso, String tiposala,
                                          LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        List<Sala> todasAsSalas = salaRepository.findByAtivaTrueOrderByTipoAscNumeroAsc();
        List<Sala> salasFiltradas = todasAsSalas.stream()
                .filter(sala -> andar == null || andar.isEmpty() || (sala.getLocalizacao() != null && sala.getLocalizacao().startsWith(andar)))
                .filter(sala -> tiposala == null || tiposala.isEmpty() || sala.getTipoSala().name().equals(tiposala))
                .filter(sala -> recurso == null || recurso.isEmpty() || sala.getRecursosAsString().contains(recurso))
                .collect(Collectors.toList());

        if (data == null || horaInicio == null || horaFim == null) {
            return salasFiltradas;
        }

        List<Sala> salasDisponiveis = new ArrayList<>();
        
        for (Sala sala : salasFiltradas) {
            Reserva dummyReserva = new Reserva();
            dummyReserva.setNumero(sala.getNumero());
            dummyReserva.setData(data);
            dummyReserva.setHora(horaInicio);
            dummyReserva.setHoraFim(horaFim);

            if (!reservaService.temConflito(dummyReserva)) {
                salasDisponiveis.add(sala);
            }
        }
        
        return salasDisponiveis; 
    }
}