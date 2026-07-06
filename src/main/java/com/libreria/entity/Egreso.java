package com.libreria.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "egresos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Egreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String concepto;

    @Column(name = "importe_moneda_local", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMonedaLocal;

    @Column(name = "importe_usd", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeUsd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_cambio_id", nullable = false)
    private TipoCambio tipoCambio;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
