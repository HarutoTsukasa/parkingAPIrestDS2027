package com.htx.app.apirest.parking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plaza")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Plaza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoPlaza estado;

    public enum EstadoPlaza {
        LIBRE, OCUPADA
    }
}