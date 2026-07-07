package com.libreria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libreria.dto.EgresoRequestDto;
import com.libreria.dto.EgresoResponseDto;
import com.libreria.entity.Egreso;
import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;
import com.libreria.repository.EgresoRepository;
import com.libreria.repository.PaisRepository;

@ExtendWith(MockitoExtension.class)
class EgresoServiceTest {

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private TipoCambioService tipoCambioService;

    @Mock
    private EgresoRepository egresoRepository;

    private EgresoService egresoService;

    private final Pais argentina = new Pais(1L, "Argentina", "ARS", "Peso argentino");

    @BeforeEach
    void setUp() {
        egresoService = new EgresoService(paisRepository, tipoCambioService, egresoRepository);
    }

    @Test
    void registrarEgreso_conDatosValidos_calculaElImporteUsdYPersisteElEgreso() {
        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.of(2026, 7, 1));
        request.setConcepto("Impuesto de exportación");
        request.setImporteMonedaLocal(new BigDecimal("1000.00"));
        request.setPaisId(1L);

        TipoCambio tipoCambio = new TipoCambio(10L, argentina, new BigDecimal("0.001100"),
                LocalDateTime.now(), FuenteTipoCambio.API);

        when(paisRepository.findById(1L)).thenReturn(Optional.of(argentina));
        when(tipoCambioService.obtenerTipoCambio(argentina)).thenReturn(tipoCambio);
        when(egresoRepository.save(any(Egreso.class))).thenAnswer(invocacion -> {
            Egreso egreso = invocacion.getArgument(0);
            egreso.setId(100L);
            return egreso;
        });

        EgresoResponseDto response = egresoService.registrarEgreso(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getImporteUsd()).isEqualByComparingTo("1.10");
        assertThat(response.getImporteMonedaLocal()).isEqualByComparingTo("1000.00");
        assertThat(response.getMonedaCodigo()).isEqualTo("ARS");
        assertThat(response.getPaisNombre()).isEqualTo("Argentina");
        assertThat(response.getConcepto()).isEqualTo("Impuesto de exportación");
        verify(egresoRepository).save(any(Egreso.class));
    }

    @Test
    void registrarEgreso_conPaisInexistente_lanzaPaisNoEncontradoException() {
        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.now());
        request.setConcepto("Arancel aduanero");
        request.setImporteMonedaLocal(new BigDecimal("500.00"));
        request.setPaisId(999L);

        when(paisRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> egresoService.registrarEgreso(request))
                .isInstanceOf(PaisNoEncontradoException.class);
    }
}
