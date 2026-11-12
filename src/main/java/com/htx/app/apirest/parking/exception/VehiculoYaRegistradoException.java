package com.htx.app.apirest.parking.exception;

public class VehiculoYaRegistradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VehiculoYaRegistradoException(String placa) {
        super("El vehículo con placa " + placa + " ya está registrado (ingresado) en el parqueadero.");
    }
}