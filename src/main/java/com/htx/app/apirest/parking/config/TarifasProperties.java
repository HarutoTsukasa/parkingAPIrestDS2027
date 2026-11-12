package com.htx.app.apirest.parking.config;

import com.htx.app.apirest.parking.model.TipoVehiculo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Tarifas por tipo de vehículo (COP).
 * Bindea {@code parking.tarifa.*} de application.properties.
 */
@Component
@ConfigurationProperties(prefix = "parking.tarifa")
@Getter @Setter
public class TarifasProperties {

    /** Duración de una fracción de cobro, en minutos. */
    private int fraccionMinutos = 15;

    private Tipos tipos = new Tipos();

    @Getter @Setter
    public static class Tipos {
        private Tarifa carro = new Tarifa();
        private Tarifa moto = new Tarifa();
        private Tarifa bicicleta = new Tarifa();
    }

    @Getter @Setter
    public static class Tarifa {
        /** Tarifa por hora en COP (informativa, se muestra al cliente). */
        private BigDecimal hora = BigDecimal.ZERO;
        /** Tarifa por fracción en COP (autoritativa para el cálculo). */
        private BigDecimal fraccion = BigDecimal.ZERO;
    }

    public Tarifa porTipo(TipoVehiculo tipo) {
        return switch (tipo) {
            case CARRO -> tipos.carro;
            case MOTO -> tipos.moto;
            case BICICLETA -> tipos.bicicleta;
        };
    }

    @PostConstruct
    void validar() {
        if (fraccionMinutos <= 0) {
            throw new IllegalStateException("parking.tarifa.fraccion-minutos debe ser > 0");
        }
        for (TipoVehiculo t : TipoVehiculo.values()) {
            Tarifa tarifa = porTipo(t);
            if (tarifa.getHora().signum() < 0) {
                throw new IllegalStateException("Tarifa por hora negativa para " + t);
            }
            if (tarifa.getFraccion().signum() <= 0) {
                throw new IllegalStateException("Tarifa por fracción debe ser > 0 para " + t);
            }
        }
    }
}