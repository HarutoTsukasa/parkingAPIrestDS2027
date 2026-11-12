package com.htx.app.apirest.parking.controller;

import com.htx.app.apirest.parking.dto.*;
import com.htx.app.apirest.parking.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final ParkingService parkingService;

    @PostMapping("/ingreso")
    public ResponseEntity<SalidaResponse> ingresar(@Valid @RequestBody IngresoRequest request) {
        SalidaResponse response = parkingService.registrarIngreso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/salida/{placa}")
    public ResponseEntity<SalidaResponse> salir(@PathVariable String placa) {
        SalidaResponse response = parkingService.registrarSalida(placa);
        return ResponseEntity.ok(response);
    }
}