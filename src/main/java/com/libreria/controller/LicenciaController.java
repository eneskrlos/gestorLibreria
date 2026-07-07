package com.libreria.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.libreria.dto.LicenciaActivarRequestDto;
import com.libreria.dto.LicenciaEstadoResponseDto;
import com.libreria.dto.LicenciaResponseDto;
import com.libreria.entity.Licencia;
import com.libreria.service.LicenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/licencia")
@RequiredArgsConstructor
public class LicenciaController {

    private final LicenciaService licenciaService;

    @GetMapping("/estado")
    public ResponseEntity<LicenciaEstadoResponseDto> obtenerEstado() {
        boolean vigente = licenciaService.validarLicencia();
        return ResponseEntity.ok(new LicenciaEstadoResponseDto(vigente));
    }

    @PostMapping("/activar")
    public ResponseEntity<LicenciaResponseDto> activarLicencia(@Valid @RequestBody LicenciaActivarRequestDto request) {
        Licencia licencia = licenciaService.activarLicencia(request.getClave());
        return ResponseEntity.ok(toResponseDto(licencia));
    }

    private LicenciaResponseDto toResponseDto(Licencia licencia) {
        return new LicenciaResponseDto(
                licencia.getClave(),
                licencia.getFechaInicio(),
                licencia.getFechaVencimiento(),
                licencia.getTipo(),
                licencia.getActiva());
    }
}
