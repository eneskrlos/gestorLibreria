package com.libreria.service.exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ExchangeRateApiClient {

    private final RestTemplate restTemplate;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Value("${exchange.api.key}")
    private String apiKey;

    public ExchangeRateApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal obtenerValorUsd(String monedaCodigo) {
        ExchangeRateApiResponse response;
        try {
            String url = apiUrl.replace("{API_KEY}", apiKey);
            response = restTemplate.getForObject(url, ExchangeRateApiResponse.class);
        } catch (RestClientException e) {
            throw new ExchangeRateApiException("No se pudo conectar con la API de tipo de cambio", e);
        }

        if (response == null || response.getConversionRates() == null) {
            throw new ExchangeRateApiException("La API de tipo de cambio devolvió una respuesta vacía");
        }

        BigDecimal tasaRespectoUsd = response.getConversionRates().get(monedaCodigo);
        if (tasaRespectoUsd == null) {
            throw new ExchangeRateApiException("La moneda '" + monedaCodigo + "' no está disponible en la API de tipo de cambio");
        }

        return BigDecimal.ONE.divide(tasaRespectoUsd, 6, RoundingMode.HALF_UP);
    }
}
