package com.htx.app.apirest.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter @Builder
public class DisponibilidadResponse {
    private int totalPlazas;
    private int libres;
    private int ocupadas;
    private Map<String, Integer> libresPorTipo;
    private Map<String, Integer> ocupadasPorTipo;
    private List<PlazaResumenResponse> plazasLibres;
    private List<PlazaResumenResponse> plazasOcupadas;
}