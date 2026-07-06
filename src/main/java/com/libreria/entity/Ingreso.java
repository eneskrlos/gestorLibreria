package com.libreria.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Immutable;

// Nombre de tabla/columnas pendiente de confirmar con la BD del sistema externo (spec.md 2.4)
@Entity
@Table(name = "ingresos")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingreso {

    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "numero_factura", nullable = false)
    private String numeroFactura;

    @Column(name = "importe_libro", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeLibro;

    @Column(name = "gasto_envio", nullable = false, precision = 12, scale = 2)
    private BigDecimal gastoEnvio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")
    private Pais pais;
}
