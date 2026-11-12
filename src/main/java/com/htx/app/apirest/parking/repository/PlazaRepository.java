package com.htx.app.apirest.parking.repository;

import com.htx.app.apirest.parking.model.Plaza;
import com.htx.app.apirest.parking.model.TipoVehiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlazaRepository extends JpaRepository<Plaza, Long> {

    List<Plaza> findByEstado(Plaza.EstadoPlaza estado);

    /**
     * Bloqueo pesimista: impide que dos transacciones lean y asignen la misma plaza.
     * Filtra por estado y tipo de vehículo; ordena por número para asignar siempre la más baja disponible.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Plaza p WHERE p.estado = :estado AND p.tipo = :tipo ORDER BY p.numero ASC")
    List<Plaza> findLibresPorTipoWithLock(@Param("estado") Plaza.EstadoPlaza estado,
                                          @Param("tipo") TipoVehiculo tipo);
}