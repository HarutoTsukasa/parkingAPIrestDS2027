package com.htx.app.apirest.parking.exception;

import com.htx.app.apirest.parking.model.TipoVehiculo;

public class PlazaOcupadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PlazaOcupadaException(TipoVehiculo tipo) {
        super("No hay plazas libres disponibles para vehículos de tipo " + tipo + ".");
    }
}