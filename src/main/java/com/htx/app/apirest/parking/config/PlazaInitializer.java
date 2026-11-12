package com.htx.app.apirest.parking.config;

import com.htx.app.apirest.parking.model.Plaza;
import com.htx.app.apirest.parking.model.TipoVehiculo;
import com.htx.app.apirest.parking.repository.PlazaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Siembra las plazas iniciales al arrancar, solo si la tabla está vacía.
 * Numeración consecutiva: carros 1..N, motos N+1..N+M, bicicletas N+M+1..N+M+K.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlazaInitializer implements ApplicationRunner {

    private final PlazaRepository plazaRepository;
    private final EspaciosInicialesProperties props;

    @Override
    public void run(ApplicationArguments args) {
        long existentes = plazaRepository.count();
        if (existentes > 0) {
            log.info("PlazaInitializer: {} plazas ya existen, no se siembra nada.", existentes);
            return;
        }

        List<Plaza> plazas = new ArrayList<>();
        int numero = 1;

        for (int i = 0; i < props.getCarro(); i++) {
            plazas.add(nueva(numero++, TipoVehiculo.CARRO));
        }
        for (int i = 0; i < props.getMoto(); i++) {
            plazas.add(nueva(numero++, TipoVehiculo.MOTO));
        }
        for (int i = 0; i < props.getBicicleta(); i++) {
            plazas.add(nueva(numero++, TipoVehiculo.BICICLETA));
        }

        plazaRepository.saveAll(plazas);
        log.info("PlazaInitializer: sembradas {} plazas (carro={}, moto={}, bicicleta={}).",
                plazas.size(), props.getCarro(), props.getMoto(), props.getBicicleta());
    }

    private Plaza nueva(int numero, TipoVehiculo tipo) {
        return Plaza.builder()
                .numero(numero)
                .tipo(tipo)
                .estado(Plaza.EstadoPlaza.LIBRE)
                .build();
    }
}