package com.libreria.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.libreria.dto.PaisResponseDto;
import com.libreria.entity.Pais;
import com.libreria.repository.PaisRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/paises")
@RequiredArgsConstructor
public class PaisController {

    private final PaisRepository paisRepository;

    @GetMapping
    public ResponseEntity<List<PaisResponseDto>> listarPaises() {
        List<PaisResponseDto> paises = paisRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(paises);
    }

    private PaisResponseDto toResponseDto(Pais pais) {
        return new PaisResponseDto(pais.getId(), pais.getNombre(), pais.getMonedaCodigo(), pais.getMonedaNombre());
    }
}
