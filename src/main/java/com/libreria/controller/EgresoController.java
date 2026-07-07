package com.libreria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.libreria.dto.EgresoRequestDto;
import com.libreria.dto.EgresoResponseDto;
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
}
