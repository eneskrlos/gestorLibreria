package com.libreria.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import com.libreria.service.LicenciaNoEncontradaException;
import com.libreria.service.LicenciaVencidaException;
import com.libreria.service.PaisNoEncontradoException;
import com.libreria.service.TipoCambioNoDisponibleException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarDatosInvalidos_retorna400ConLosCamposInvalidos() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "egresoRequestDto");
        bindingResult.addError(new FieldError("egresoRequestDto", "concepto", "must not be blank"));
        bindingResult.addError(new FieldError("egresoRequestDto", "fecha", "must not be null"));
        BindException ex = new BindException(bindingResult);

        ResponseEntity<ErrorResponseDto> response = handler.manejarDatosInvalidos(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Datos inválidos");
        assertThat(response.getBody().mensaje()).contains("concepto").contains("fecha");
    }

    @Test
    void manejarEntidadNoEncontrada_conPaisNoEncontrado_retorna404ConElMensajeOriginal() {
        PaisNoEncontradoException ex = new PaisNoEncontradoException("No se encontró el país con id 99");

        ResponseEntity<ErrorResponseDto> response = handler.manejarEntidadNoEncontrada(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().mensaje()).isEqualTo("No se encontró el país con id 99");
    }

    @Test
    void manejarEntidadNoEncontrada_conLicenciaNoEncontrada_retorna404ConElMensajeOriginal() {
        LicenciaNoEncontradaException ex = new LicenciaNoEncontradaException(
                "No se encontró ninguna licencia con la clave ingresada.");

        ResponseEntity<ErrorResponseDto> response = handler.manejarEntidadNoEncontrada(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().mensaje()).isEqualTo("No se encontró ninguna licencia con la clave ingresada.");
    }

    @Test
    void manejarTipoCambioNoDisponible_retorna503ConElMensajeOriginal() {
        TipoCambioNoDisponibleException ex = new TipoCambioNoDisponibleException(
                "No hay tipo de cambio disponible para Argentina y no se pudo consultar la API");

        ResponseEntity<ErrorResponseDto> response = handler.manejarTipoCambioNoDisponible(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().error()).isEqualTo("Servicio no disponible");
        assertThat(response.getBody().mensaje())
                .isEqualTo("No hay tipo de cambio disponible para Argentina y no se pudo consultar la API");
    }

    @Test
    void manejarLicenciaVencida_retorna400ConElMensajeOriginal() {
        LicenciaVencidaException ex = new LicenciaVencidaException(
                "La licencia ingresada ya está vencida. Contacte al administrador para renovarla.");

        ResponseEntity<ErrorResponseDto> response = handler.manejarLicenciaVencida(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("Licencia vencida");
        assertThat(response.getBody().mensaje())
                .isEqualTo("La licencia ingresada ya está vencida. Contacte al administrador para renovarla.");
    }
}
