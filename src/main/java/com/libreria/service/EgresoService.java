package com.libreria.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.libreria.dto.EgresoRequestDto;
import com.libreria.dto.EgresoResponseDto;
import com.libreria.dto.ReporteEgresosDto;
import com.libreria.dto.ReporteFilterDto;
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

    public List<ReporteEgresosDto> generarReporte(ReporteFilterDto filtro) {
        List<Egreso> egresos = egresoRepository.buscarPorFiltros(
                filtro.getFechaInicio(), filtro.getFechaFin(), filtro.getPaisId());

        Map<Long, List<Egreso>> egresosPorPais = egresos.stream()
                .collect(Collectors.groupingBy(e -> e.getPais().getId(), LinkedHashMap::new, Collectors.toList()));

        return egresosPorPais.values().stream()
                .map(this::toReporteEgresosDto)
                .collect(Collectors.toList());
    }

    private ReporteEgresosDto toReporteEgresosDto(List<Egreso> egresosDelPais) {
        Pais pais = egresosDelPais.get(0).getPais();

        BigDecimal totalMonedaLocal = egresosDelPais.stream()
                .map(Egreso::getImporteMonedaLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = egresosDelPais.stream()
                .map(Egreso::getImporteUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EgresoResponseDto> egresoDtos = egresosDelPais.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return new ReporteEgresosDto(pais.getNombre(), pais.getMonedaCodigo(), egresoDtos, totalMonedaLocal, totalUsd);
    }
}
