package com.htx.app.apirest.parking.exception;

public class PlazaOcupadaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlazaOcupadaException() {
		super("No hay plazas libres disponibles en este momento.");
	}
}