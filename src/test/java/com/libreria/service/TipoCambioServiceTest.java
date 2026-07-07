package com.libreria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;
import com.libreria.repository.TipoCambioRepository;
import com.libreria.service.exchange.ExchangeRateApiClient;
import com.libreria.service.exchange.ExchangeRateApiException;

@ExtendWith(MockitoExtension.class)
class TipoCambioServiceTest {

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @Mock
    private TipoCambioRepository tipoCambioRepository;

    private TipoCambioService tipoCambioService;

    private final Pais argentina = new Pais(1L, "Argentina", "ARS", "Peso argentino");

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        tipoCambioService = new TipoCambioService(exchangeRateApiClient, tipoCambioRepository);
    }

    @Test
    void obtenerTipoCambio_conApiDisponible_guardaConFuenteApi() {
        when(exchangeRateApiClient.obtenerValorUsd("ARS")).thenReturn(new BigDecimal("0.001100"));
        when(tipoCambioRepository.save(any(TipoCambio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoCambio resultado = tipoCambioService.obtenerTipoCambio(argentina);

        assertThat(resultado.getFuente()).isEqualTo(FuenteTipoCambio.API);
        assertThat(resultado.getValorUsd()).isEqualByComparingTo("0.001100");
        assertThat(resultado.getPais()).isEqualTo(argentina);
        verify(tipoCambioRepository).save(any(TipoCambio.class));
    }

    @Test
    void obtenerTipoCambio_conApiCaidaYUltimoRegistroEnBd_guardaConFuenteManualUsandoElUltimoValor() {
        when(exchangeRateApiClient.obtenerValorUsd("ARS"))
                .thenThrow(new ExchangeRateApiException("timeout"));
        TipoCambio ultimo = new TipoCambio(5L, argentina, new BigDecimal("0.001000"),
                LocalDateTime.now().minusDays(1), FuenteTipoCambio.API);
        when(tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(argentina))
                .thenReturn(Optional.of(ultimo));
        when(tipoCambioRepository.save(any(TipoCambio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoCambio resultado = tipoCambioService.obtenerTipoCambio(argentina);

        assertThat(resultado.getFuente()).isEqualTo(FuenteTipoCambio.MANUAL);
        assertThat(resultado.getValorUsd()).isEqualByComparingTo("0.001000");
    }

    @Test
    void obtenerTipoCambio_conApiCaidaYSinRegistroPrevio_lanzaTipoCambioNoDisponibleException() {
        when(exchangeRateApiClient.obtenerValorUsd("ARS"))
                .thenThrow(new ExchangeRateApiException("timeout"));
        when(tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(argentina))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoCambioService.obtenerTipoCambio(argentina))
                .isInstanceOf(TipoCambioNoDisponibleException.class);
    }

    @Test
    void guardarTipoCambio_persisteElValorYFuenteIndicados() {
        when(tipoCambioRepository.save(any(TipoCambio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoCambio resultado = tipoCambioService.guardarTipoCambio(argentina, new BigDecimal("0.001200"),
                FuenteTipoCambio.MANUAL);

        assertThat(resultado.getPais()).isEqualTo(argentina);
        assertThat(resultado.getValorUsd()).isEqualByComparingTo("0.001200");
        assertThat(resultado.getFuente()).isEqualTo(FuenteTipoCambio.MANUAL);
        assertThat(resultado.getFechaConsulta()).isNotNull();
    }
}
