package com.htx.app.apirest.parking.repository;

import com.htx.app.apirest.parking.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
	Optional<Vehiculo> findByPlaca(String placa);

	boolean existsByPlaca(String placa);
}