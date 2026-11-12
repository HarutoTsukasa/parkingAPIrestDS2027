package com.htx.app.apirest.parking.repository;

import com.htx.app.apirest.parking.model.Plaza;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlazaRepository extends JpaRepository<Plaza, Long> {

    List<Plaza> findByEstado(Plaza.EstadoPlaza estado);

    /**
     * Bloqueo pesimista: impide que dos transacciones lean y asignen la misma plaza.
     * Es la pieza que evita la doble asignación en concurrencia.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Plaza p WHERE p.estado = :estado ORDER BY p.numero ASC")
    List<Plaza> findFirstByEstadoWithLock(Plaza.EstadoPlaza estado);
}