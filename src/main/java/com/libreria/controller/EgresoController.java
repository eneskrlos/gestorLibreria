package com.libreria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.libreria.dto.EgresoRequestDto;
import com.libreria.dto.EgresoResponseDto;
import com.libreria.dto.ReporteEgresosDto;
import com.libreria.dto.ReporteFilterDto;
import com.libreria.service.EgresoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/egresos")
@RequiredArgsConstructor
public class EgresoController {

    private final EgresoService egresoService;

    @PostMapping
    public ResponseEntity<EgresoResponseDto> registrarEgreso(@Valid @RequestBody EgresoRequestDto request) {
        EgresoResponseDto response = egresoService.registrarEgreso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<ReporteEgresosDto>> obtenerReporte(@Valid @ModelAttribute ReporteFilterDto filtro) {
        List<ReporteEgresosDto> reporte = egresoService.generarReporte(filtro);
        return ResponseEntity.ok(reporte);
    }
}
