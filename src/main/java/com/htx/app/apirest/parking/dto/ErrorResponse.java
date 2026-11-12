package com.htx.app.apirest.parking.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class ErrorResponse {
    private int codigo;
    private String mensaje;
    private LocalDateTime timestamp;
}