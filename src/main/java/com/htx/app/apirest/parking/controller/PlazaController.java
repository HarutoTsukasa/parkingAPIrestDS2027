package com.htx.app.apirest.parking.controller;

import com.htx.app.apirest.parking.dto.DisponibilidadResponse;
import com.htx.app.apirest.parking.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plazas")
@RequiredArgsConstructor
public class PlazaController {

	private final ParkingService parkingService;

	@GetMapping("/disponibilidad")
	public ResponseEntity<DisponibilidadResponse> disponibilidad() {
		return ResponseEntity.ok(parkingService.consultarDisponibilidad());
	}
}