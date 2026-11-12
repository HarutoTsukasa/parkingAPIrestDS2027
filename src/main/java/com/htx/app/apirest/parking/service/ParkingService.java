package com.htx.app.apirest.parking.service;

import com.htx.app.apirest.parking.dto.*;

public interface ParkingService {
	SalidaResponse registrarIngreso(IngresoRequest request);

	SalidaResponse registrarSalida(String placa);

	DisponibilidadResponse consultarDisponibilidad();
}