package com.libreria.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIngresosDto {

    private String paisNombre;
    private String monedaCodigo;
    private List<IngresoResponseDto> ingresos;
    private BigDecimal totalGeneral;
}
