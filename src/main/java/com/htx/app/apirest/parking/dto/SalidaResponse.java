package com.htx.app.apirest.parking.dto;

import com.htx.app.apirest.parking.model.TipoVehiculo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SalidaResponse {
	private String placa;
	private TipoVehiculo tipo;
	private Integer plazaNumero;
	private LocalDateTime fechaIngreso;
	private LocalDateTime fechaSalida;
	private long fraccionesCobradas;
	private BigDecimal tarifaHoraCop;
	private BigDecimal tarifaFraccionCop;
	private BigDecimal totalCop;
}