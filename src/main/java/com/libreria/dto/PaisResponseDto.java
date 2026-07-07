package com.libreria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaisResponseDto {

    private Long id;
    private String nombre;
    private String monedaCodigo;
    private String monedaNombre;
}
