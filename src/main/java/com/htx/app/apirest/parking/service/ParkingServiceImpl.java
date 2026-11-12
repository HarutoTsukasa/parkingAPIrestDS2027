package com.htx.app.apirest.parking.service;

import com.htx.app.apirest.parking.config.TarifasProperties;
import com.htx.app.apirest.parking.dto.*;
import com.htx.app.apirest.parking.exception.*;
import com.htx.app.apirest.parking.model.Plaza;
import com.htx.app.apirest.parking.model.TipoVehiculo;
import com.htx.app.apirest.parking.model.Vehiculo;
import com.htx.app.apirest.parking.repository.PlazaRepository;
import com.htx.app.apirest.parking.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

	private final VehiculoRepository vehiculoRepository;
	private final PlazaRepository plazaRepository;
	private final TarifasProperties tarifas;

	@Override
	@Transactional
	public SalidaResponse registrarIngreso(IngresoRequest request) {
		String placa = request.getPlaca().toUpperCase().trim();
		TipoVehiculo tipo = request.getTipo();

		if (vehiculoRepository.existsByPlaca(placa)) {
			throw new VehiculoYaRegistradoException(placa);
		}

		List<Plaza> libres = plazaRepository.findLibresPorTipoWithLock(Plaza.EstadoPlaza.LIBRE, tipo);
		if (libres.isEmpty()) {
			throw new PlazaOcupadaException(tipo);
		}

		Plaza plaza = libres.get(0);
		plaza.setEstado(Plaza.EstadoPlaza.OCUPADA);
		plazaRepository.save(plaza);

		Vehiculo vehiculo = Vehiculo.builder().placa(placa).tipo(tipo).fechaIngreso(LocalDateTime.now()).plaza(plaza)
				.build();
		vehiculoRepository.save(vehiculo);

		TarifasProperties.Tarifa tarifa = tarifas.porTipo(tipo);

		return SalidaResponse.builder().placa(placa).tipo(tipo).plazaNumero(plaza.getNumero())
				.fechaIngreso(vehiculo.getFechaIngreso()).fraccionesCobradas(0L).tarifaHoraCop(tarifa.getHora())
				.tarifaFraccionCop(tarifa.getFraccion()).totalCop(BigDecimal.ZERO).build();
	}

	@Override
	@Transactional
	public SalidaResponse registrarSalida(String placa) {
		String placaNormalizada = placa.toUpperCase().trim();
		Vehiculo vehiculo = vehiculoRepository.findByPlaca(placaNormalizada)
				.orElseThrow(() -> new VehiculoNoRegistradoException(placaNormalizada));

		LocalDateTime fechaSalida = LocalDateTime.now();
		long segundosPermanencia = Duration.between(vehiculo.getFechaIngreso(), fechaSalida).getSeconds();
		long segundosPorFraccion = (long) tarifas.getFraccionMinutos() * 60L;

		long fracciones = (segundosPermanencia + segundosPorFraccion - 1) / segundosPorFraccion;

		TarifasProperties.Tarifa tarifa = tarifas.porTipo(vehiculo.getTipo());

		BigDecimal totalCop = tarifa.getFraccion().multiply(BigDecimal.valueOf(fracciones)).setScale(0,
				RoundingMode.HALF_UP);

		Plaza plaza = vehiculo.getPlaza();
		plaza.setEstado(Plaza.EstadoPlaza.LIBRE);
		plazaRepository.save(plaza);

		vehiculoRepository.delete(vehiculo);

		return SalidaResponse.builder().placa(placaNormalizada).tipo(vehiculo.getTipo()).plazaNumero(plaza.getNumero())
				.fechaIngreso(vehiculo.getFechaIngreso()).fechaSalida(fechaSalida).fraccionesCobradas(fracciones)
				.tarifaHoraCop(tarifa.getHora()).tarifaFraccionCop(tarifa.getFraccion()).totalCop(totalCop).build();
	}

	@Override
	@Transactional(readOnly = true)
	public DisponibilidadResponse consultarDisponibilidad() {
		List<Plaza> todas = plazaRepository.findAll();

		List<PlazaResumenResponse> libres = todas.stream().filter(p -> p.getEstado() == Plaza.EstadoPlaza.LIBRE)
				.map(this::resumen).sorted((a, b) -> Integer.compare(a.getNumero(), b.getNumero())).toList();

		List<PlazaResumenResponse> ocupadas = todas.stream().filter(p -> p.getEstado() == Plaza.EstadoPlaza.OCUPADA)
				.map(this::resumen).sorted((a, b) -> Integer.compare(a.getNumero(), b.getNumero())).toList();

		return DisponibilidadResponse.builder().totalPlazas(todas.size()).libres(libres.size())
				.ocupadas(ocupadas.size()).libresPorTipo(contarPorTipo(libres)).ocupadasPorTipo(contarPorTipo(ocupadas))
				.plazasLibres(libres).plazasOcupadas(ocupadas).build();
	}

	private PlazaResumenResponse resumen(Plaza p) {
		return PlazaResumenResponse.builder().numero(p.getNumero()).tipo(p.getTipo()).build();
	}

	private Map<String, Integer> contarPorTipo(List<PlazaResumenResponse> plazas) {
		Map<String, Integer> conteo = new LinkedHashMap<>();
		for (TipoVehiculo t : TipoVehiculo.values()) {
			conteo.put(t.name(), 0);
		}
		plazas.forEach(p -> conteo.merge(p.getTipo().name(), 1, Integer::sum));
		return conteo;
	}
}