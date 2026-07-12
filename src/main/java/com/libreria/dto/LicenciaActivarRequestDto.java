package com.libreria.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LicenciaActivarRequestDto {

    @NotBlank
    private String clave;
}
