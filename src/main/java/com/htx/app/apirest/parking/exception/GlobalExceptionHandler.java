package com.htx.app.apirest.parking.exception;

import com.htx.app.apirest.parking.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(VehiculoYaRegistradoException.class)
	public ResponseEntity<ErrorResponse> handleVehiculoYaRegistrado(VehiculoYaRegistradoException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(PlazaOcupadaException.class)
	public ResponseEntity<ErrorResponse> handlePlazaOcupada(PlazaOcupadaException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(VehiculoNoRegistradoException.class)
	public ResponseEntity<ErrorResponse> handleVehiculoNoRegistrado(VehiculoNoRegistradoException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, mensaje);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
		String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
		return build(HttpStatus.BAD_REQUEST, "Cuerpo de petición inválido: " + detalle);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje) {
		ErrorResponse body = ErrorResponse.builder().codigo(status.value()).mensaje(mensaje)
				.timestamp(LocalDateTime.now()).build();
		return ResponseEntity.status(status).body(body);
	}
}