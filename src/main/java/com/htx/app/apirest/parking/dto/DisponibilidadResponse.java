package com.htx.app.apirest.parking.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter @Builder
public class DisponibilidadResponse {
    private int totalPlazas;
    private int libres;
    private int ocupadas;
    private List<Integer> numerosLibres;
    private List<Integer> numerosOcupados;
}