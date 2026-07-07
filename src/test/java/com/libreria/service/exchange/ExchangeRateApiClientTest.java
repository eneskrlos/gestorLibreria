package com.libreria.service.exchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ExchangeRateApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ExchangeRateApiClient client;

    @BeforeEach
    void setUp() {
        client = new ExchangeRateApiClient(restTemplate);
        ReflectionTestUtils.setField(client, "apiUrl", "https://v6.exchangerate-api.com/v6/{API_KEY}/latest/USD");
        ReflectionTestUtils.setField(client, "apiKey", "clave-de-prueba");
    }

    @Test
    void obtenerValorUsd_conMonedaDisponible_calculaElInversoCorrectamente() {
        ExchangeRateApiResponse response = new ExchangeRateApiResponse();
        response.setResult("success");
        response.setConversionRates(Map.of("ARS", new BigDecimal("1000")));
        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);

        BigDecimal valorUsd = client.obtenerValorUsd("ARS");

        assertThat(valorUsd).isEqualByComparingTo("0.001000");
    }

    @Test
    void obtenerValorUsd_conFalloDeConexion_lanzaExchangeRateApiException() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> client.obtenerValorUsd("ARS"))
                .isInstanceOf(ExchangeRateApiException.class);
    }

    @Test
    void obtenerValorUsd_conRespuestaVacia_lanzaExchangeRateApiException() {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);

        assertThatThrownBy(() -> client.obtenerValorUsd("ARS"))
                .isInstanceOf(ExchangeRateApiException.class);
    }

    @Test
    void obtenerValorUsd_conMonedaNoDisponibleEnLaApi_lanzaExchangeRateApiException() {
        ExchangeRateApiResponse response = new ExchangeRateApiResponse();
        response.setResult("success");
        response.setConversionRates(Map.of("BRL", new BigDecimal("5")));
        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);

        assertThatThrownBy(() -> client.obtenerValorUsd("ARS"))
                .isInstanceOf(ExchangeRateApiException.class);
    }
}
