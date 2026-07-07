package com.libreria.service.exchange;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class ExchangeRateApiResponse {

    private String result;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;
}
