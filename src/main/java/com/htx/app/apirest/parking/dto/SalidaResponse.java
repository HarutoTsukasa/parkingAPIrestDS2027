package com.htx.app.apirest.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class SalidaResponse {
    private String placa;
    private Integer plazaNumero;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private long horasCobradas;
    private BigDecimal totalCobrado;
}