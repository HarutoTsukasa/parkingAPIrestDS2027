package com.htx.app.apirest.parking.dto;

import com.htx.app.apirest.parking.model.TipoVehiculo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlazaResumenResponse {
	private int numero;
	private TipoVehiculo tipo;
}