package com.libreria.dto;

import java.time.LocalDate;

import com.libreria.entity.TipoLicencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaResponseDto {

    private String clave;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private TipoLicencia tipo;
    private Boolean activa;
}
