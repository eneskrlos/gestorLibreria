package com.libreria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EgresoResponseDto {

    private Long id;
    private LocalDate fecha;
    private String concepto;
    private BigDecimal importeMonedaLocal;
    private String monedaCodigo;
    private BigDecimal importeUsd;
    private String paisNombre;
    private LocalDateTime fechaRegistro;
}
