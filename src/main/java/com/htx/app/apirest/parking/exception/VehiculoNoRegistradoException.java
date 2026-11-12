package com.htx.app.apirest.parking.exception;

public class VehiculoNoRegistradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public VehiculoNoRegistradoException(String placa) {
		super("No existe un vehículo registrado con la placa " + placa + ".");
	}
}