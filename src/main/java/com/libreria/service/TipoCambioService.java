package com.libreria.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;
import com.libreria.repository.TipoCambioRepository;
import com.libreria.service.exchange.ExchangeRateApiClient;
import com.libreria.service.exchange.ExchangeRateApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoCambioService {

    private final ExchangeRateApiClient exchangeRateApiClient;
    private final TipoCambioRepository tipoCambioRepository;

    public TipoCambio obtenerTipoCambio(Pais pais) {
        try {
            BigDecimal valorUsd = exchangeRateApiClient.obtenerValorUsd(pais.getMonedaCodigo());
            return guardarTipoCambio(pais, valorUsd, FuenteTipoCambio.API);
        } catch (ExchangeRateApiException e) {
            log.warn("Fallo la API de tipo de cambio para el pais '{}': {}. Se usara el ultimo tipo de cambio guardado.",
                    pais.getNombre(), e.getMessage());

            TipoCambio ultimoConocido = tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(pais)
                    .orElseThrow(() -> new TipoCambioNoDisponibleException(
                            "No se pudo obtener el tipo de cambio actualizado para " + pais.getNombre()
                                    + ". Intente nuevamente más tarde."));

            return guardarTipoCambio(pais, ultimoConocido.getValorUsd(), FuenteTipoCambio.MANUAL);
        }
    }

    public TipoCambio guardarTipoCambio(Pais pais, BigDecimal valor, FuenteTipoCambio fuente) {
        TipoCambio tipoCambio = new TipoCambio(null, pais, valor, LocalDateTime.now(), fuente);
        return tipoCambioRepository.save(tipoCambio);
    }
}
