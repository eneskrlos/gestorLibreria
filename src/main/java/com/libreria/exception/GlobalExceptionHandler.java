package com.libreria.exception;

import java.util.Map;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> ETIQUETAS_CAMPOS = Map.of(
            "fecha", "fecha",
            "concepto", "concepto",
            "importeMonedaLocal", "importe",
            "paisId", "país",
            "clave", "clave de la licencia",
            "fechaInicio", "fecha de inicio",
            "fechaFin", "fecha de fin");

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDto> manejarDatosInvalidos(BindException ex) {
        String campos = ex.getFieldErrors().stream()
                .map(FieldError::getField)
                .map(campo -> ETIQUETAS_CAMPOS.getOrDefault(campo, campo))
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado no controlado", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Por favor, intente nuevamente más tarde.");
    }

    private ResponseEntity<ErrorResponseDto> construirRespuesta(HttpStatus status, String error, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(error, mensaje));
    }
}
