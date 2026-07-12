package com.libreria.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.libreria.service.LicenciaNoEncontradaException;
import com.libreria.service.LicenciaVencidaException;
import com.libreria.service.PaisNoEncontradoException;
import com.libreria.service.TipoCambioNoDisponibleException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDto> manejarDatosInvalidos(BindException ex) {
        String campos = ex.getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(", "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Revise los siguientes campos: " + campos + ".");
    }

    @ExceptionHandler({ PaisNoEncontradoException.class, LicenciaNoEncontradaException.class })
    public ResponseEntity<ErrorResponseDto> manejarEntidadNoEncontrada(RuntimeException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(TipoCambioNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDto> manejarTipoCambioNoDisponible(TipoCambioNoDisponibleException ex) {
        return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage());
    }

    @ExceptionHandler(LicenciaVencidaException.class)
    public ResponseEntity<ErrorResponseDto> manejarLicenciaVencida(LicenciaVencidaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Licencia vencida", ex.getMessage());
    }

    private ResponseEntity<ErrorResponseDto> construirRespuesta(HttpStatus status, String error, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(error, mensaje));
    }
}
