package com.htx.app.apirest.parking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cantidad inicial de plazas por tipo. Solo se aplican si la tabla plaza está
 * vacía. Bindea {@code parking.espacios.iniciales.*}.
 */
@Component
@ConfigurationProperties(prefix = "parking.espacios.iniciales")
@Getter
@Setter
public class EspaciosInicialesProperties {
	private int carro = 25;
	private int moto = 20;
	private int bicicleta = 5;
}