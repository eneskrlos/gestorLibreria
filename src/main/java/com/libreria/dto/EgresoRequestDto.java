package com.libreria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EgresoRequestDto {

    @NotNull
    private LocalDate fecha;

    @NotBlank
    private String concepto;

    @NotNull
    @Positive
    private BigDecimal importeMonedaLocal;

    @NotNull
    private Long paisId;
}
