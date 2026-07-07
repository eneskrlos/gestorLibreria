package com.libreria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libreria.dto.ReporteFilterDto;
import com.libreria.dto.ReporteIngresosDto;
import com.libreria.entity.Ingreso;
import com.libreria.entity.Pais;
import com.libreria.repository.IngresoRepository;

@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    private IngresoService ingresoService;

    private final Pais argentina = new Pais(1L, "Argentina", "ARS", "Peso argentino");
    private final Pais brasil = new Pais(2L, "Brasil", "BRL", "Real brasileño");

    @BeforeEach
    void setUp() {
        ingresoService = new IngresoService(ingresoRepository);
    }

    @Test
    void generarReporte_conIngresosDeVariosPaises_agrupaYCalculaTotalGeneralPorPais() {
        Ingreso ingresoArgentina1 = new Ingreso(1L, LocalDate.of(2026, 1, 10), "F-001",
                new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("110.00"), argentina);
        Ingreso ingresoArgentina2 = new Ingreso(2L, LocalDate.of(2026, 1, 15), "F-002",
                new BigDecimal("200.00"), new BigDecimal("20.00"), new BigDecimal("220.00"), argentina);
        Ingreso ingresoBrasil = new Ingreso(3L, LocalDate.of(2026, 1, 12), "F-003",
                new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("55.00"), brasil);

        ReporteFilterDto filtro = new ReporteFilterDto();
        filtro.setFechaInicio(LocalDate.of(2026, 1, 1));
        filtro.setFechaFin(LocalDate.of(2026, 1, 31));

        when(ingresoRepository.buscarPorFiltros(filtro.getFechaInicio(), filtro.getFechaFin(), null))
                .thenReturn(List.of(ingresoArgentina1, ingresoArgentina2, ingresoBrasil));

        List<ReporteIngresosDto> reporte = ingresoService.generarReporte(filtro);

        assertThat(reporte).hasSize(2);

        ReporteIngresosDto reporteArgentina = reporte.stream()
                .filter(r -> r.getPaisNombre().equals("Argentina"))
                .findFirst().orElseThrow();
        assertThat(reporteArgentina.getMonedaCodigo()).isEqualTo("ARS");
        assertThat(reporteArgentina.getIngresos()).hasSize(2);
        assertThat(reporteArgentina.getTotalGeneral()).isEqualByComparingTo("330.00");

        ReporteIngresosDto reporteBrasil = reporte.stream()
                .filter(r -> r.getPaisNombre().equals("Brasil"))
                .findFirst().orElseThrow();
        assertThat(reporteBrasil.getMonedaCodigo()).isEqualTo("BRL");
        assertThat(reporteBrasil.getIngresos()).hasSize(1);
        assertThat(reporteBrasil.getTotalGeneral()).isEqualByComparingTo("55.00");
    }

    @Test
    void generarReporte_sinIngresosEnElRango_devuelveListaVacia() {
        ReporteFilterDto filtro = new ReporteFilterDto();
        filtro.setFechaInicio(LocalDate.of(2020, 1, 1));
        filtro.setFechaFin(LocalDate.of(2020, 12, 31));

        when(ingresoRepository.buscarPorFiltros(filtro.getFechaInicio(), filtro.getFechaFin(), null))
                .thenReturn(List.of());

        List<ReporteIngresosDto> reporte = ingresoService.generarReporte(filtro);

        assertThat(reporte).isEmpty();
    }
}
