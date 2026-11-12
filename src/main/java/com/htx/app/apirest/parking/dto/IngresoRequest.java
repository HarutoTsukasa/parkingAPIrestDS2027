package com.htx.app.apirest.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IngresoRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z0-9]{5,10}$",
             message = "La placa debe tener entre 5 y 10 caracteres alfanuméricos en mayúscula")
    private String placa;
}