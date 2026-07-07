package com.libreria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngresoResponseDto {

    private LocalDate fecha;
    private String numeroFactura;
    private BigDecimal importeLibro;
    private BigDecimal gastoEnvio;
    private BigDecimal total;
}
