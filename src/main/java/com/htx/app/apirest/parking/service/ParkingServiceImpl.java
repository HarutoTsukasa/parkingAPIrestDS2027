package com.htx.app.apirest.parking.service;

import com.htx.app.apirest.parking.dto.*;
import com.htx.app.apirest.parking.exception.*;
import com.htx.app.apirest.parking.model.Plaza;
import com.htx.app.apirest.parking.model.Vehiculo;
import com.htx.app.apirest.parking.repository.PlazaRepository;
import com.htx.app.apirest.parking.repository.VehiculoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private static final BigDecimal TARIFA_POR_HORA = new BigDecimal("2.50");

    private final VehiculoRepository vehiculoRepository;
    private final PlazaRepository plazaRepository;

    @Override
    @Transactional
    public SalidaResponse registrarIngreso(IngresoRequest request) {
        String placa = request.getPlaca().toUpperCase().trim();

        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new VehiculoYaRegistradoException(placa);
        }

        // Bloqueo pesimista: la primera transacción que llega bloquea la fila.
        // Si otra llega al mismo tiempo, espera hasta que la primera confirme o revierta.
        List<Plaza> libres = plazaRepository.findFirstByEstadoWithLock(Plaza.EstadoPlaza.LIBRE);
        if (libres.isEmpty()) {
            throw new PlazaOcupadaException();
        }

        Plaza plaza = libres.get(0);
        plaza.setEstado(Plaza.EstadoPlaza.OCUPADA);
        plazaRepository.save(plaza);

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .fechaIngreso(LocalDateTime.now())
                .plaza(plaza)
                .build();
        vehiculoRepository.save(vehiculo);

        return SalidaResponse.builder()
                .placa(placa)
                .plazaNumero(plaza.getNumero())
                .fechaIngreso(vehiculo.getFechaIngreso())
                .build();
    }

    @Override
    @Transactional
    public SalidaResponse registrarSalida(String placa) {
        String placaNormalizada = placa.toUpperCase().trim();
        Vehiculo vehiculo = vehiculoRepository.findByPlaca(placaNormalizada)
                .orElseThrow(() -> new VehiculoNoRegistradoException(placaNormalizada));

        LocalDateTime fechaSalida = LocalDateTime.now();
        long minutos = Duration.between(vehiculo.getFechaIngreso(), fechaSalida).toMinutes();
        long horasCobradas = (long) Math.ceil(minutos / 60.0);
        if (horasCobradas == 0) horasCobradas = 1; // mínimo una hora

        BigDecimal total = TARIFA_POR_HORA.multiply(BigDecimal.valueOf(horasCobradas));

        Plaza plaza = vehiculo.getPlaza();
        plaza.setEstado(Plaza.EstadoPlaza.LIBRE);
        plazaRepository.save(plaza);

        vehiculoRepository.delete(vehiculo);

        return SalidaResponse.builder()
                .placa(placaNormalizada)
                .plazaNumero(plaza.getNumero())
                .fechaIngreso(vehiculo.getFechaIngreso())
                .fechaSalida(fechaSalida)
                .horasCobradas(horasCobradas)
                .totalCobrado(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse consultarDisponibilidad() {
        List<Plaza> todas = plazaRepository.findAll();
        List<Integer> libres = todas.stream()
                .filter(p -> p.getEstado() == Plaza.EstadoPlaza.LIBRE)
                .map(Plaza::getNumero)
                .sorted()
                .toList();
        List<Integer> ocupadas = todas.stream()
                .filter(p -> p.getEstado() == Plaza.EstadoPlaza.OCUPADA)
                .map(Plaza::getNumero)
                .sorted()
                .toList();

        return DisponibilidadResponse.builder()
                .totalPlazas(todas.size())
                .libres(libres.size())
                .ocupadas(ocupadas.size())
                .numerosLibres(libres)
                .numerosOcupados(ocupadas)
                .build();
    }
}