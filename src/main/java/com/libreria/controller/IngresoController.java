package com.libreria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.libreria.dto.ReporteFilterDto;
import com.libreria.dto.ReporteIngresosDto;
import com.libreria.service.IngresoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    @GetMapping("/reporte")
    public ResponseEntity<List<ReporteIngresosDto>> obtenerReporte(@Valid @ModelAttribute ReporteFilterDto filtro) {
        List<ReporteIngresosDto> reporte = ingresoService.generarReporte(filtro);
        return ResponseEntity.ok(reporte);
    }
}
