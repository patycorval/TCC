package com.bd.sitebd.config;

import com.bd.sitebd.model.Sala;
import com.bd.sitebd.model.enums.TipoSala;
import com.bd.sitebd.model.enums.Recurso;
import com.bd.sitebd.service.SalaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InicializadorSalas implements CommandLineRunner {

        private final SalaService salaService;

        public InicializadorSalas(SalaService salaService) {
                this.salaService = salaService;
        }

        @Override
        public void run(String... args) {
                salaService.apagarTodasSalas();

                List<Sala> salas = List.of(
                                new Sala("3.01", 50, "3º andar", TipoSala.LABORATORIO, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 20),
                                new Sala("3.02", 40, "3º andar", TipoSala.LABORATORIO, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 15),
                                new Sala("3.03", 45, "3º andar", TipoSala.LABORATORIO, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.PROJETOR), 24),
                                new Sala("3.04", 60, "3º andar", TipoSala.LABORATORIO, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 25),
                                new Sala("3.05", 60, "3º andar", TipoSala.LABORATORIO, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.PROJETOR), 18),

                                // 5º andar
                                new Sala("501", 50, "5º andar", TipoSala.SALA_AULA, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.PROJETOR), 1),
                                new Sala("502", 40, "5º andar", TipoSala.SALA_AULA, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 1),
                                new Sala("503", 45, "5º andar", TipoSala.SALA_AULA, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.PROJETOR), 1),
                                new Sala("504", 60, "5º andar", TipoSala.SALA_AULA, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 1),
                                new Sala("505", 60, "5º andar", TipoSala.SALA_AULA, true,
                                                List.of(Recurso.COMPUTADORES, Recurso.TELEVISOR), 1));

                salaService.salvarTodas(salas);
                System.out.println("✅ Salas pré-definidas carregadas com sucesso.");
        }
}
