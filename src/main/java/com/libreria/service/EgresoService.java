package com.libreria.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.libreria.dto.EgresoRequestDto;
import com.libreria.dto.EgresoResponseDto;
import com.libreria.entity.Egreso;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;
import com.libreria.repository.EgresoRepository;
import com.libreria.repository.PaisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EgresoService {

    private final PaisRepository paisRepository;
    private final TipoCambioService tipoCambioService;
    private final EgresoRepository egresoRepository;

    public EgresoResponseDto registrarEgreso(EgresoRequestDto request) {
        Pais pais = paisRepository.findById(request.getPaisId())
                .orElseThrow(() -> new PaisNoEncontradoException(
                        "No se encontró el país con id " + request.getPaisId()));

        TipoCambio tipoCambio = tipoCambioService.obtenerTipoCambio(pais);

        BigDecimal importeUsd = request.getImporteMonedaLocal()
                .multiply(tipoCambio.getValorUsd())
                .setScale(2, RoundingMode.HALF_UP);

        Egreso egreso = new Egreso(null, request.getFecha(), request.getConcepto(),
                request.getImporteMonedaLocal(), importeUsd, pais, tipoCambio, LocalDateTime.now());

        Egreso guardado = egresoRepository.save(egreso);

        return toResponseDto(guardado);
    }

    private EgresoResponseDto toResponseDto(Egreso egreso) {
        return new EgresoResponseDto(
                egreso.getId(),
                egreso.getFecha(),
                egreso.getConcepto(),
                egreso.getImporteMonedaLocal(),
                egreso.getPais().getMonedaCodigo(),
                egreso.getImporteUsd(),
                egreso.getPais().getNombre(),
                egreso.getFechaRegistro());
    }
}
