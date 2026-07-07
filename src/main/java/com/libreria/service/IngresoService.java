package com.libreria.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.libreria.dto.IngresoResponseDto;
import com.libreria.dto.ReporteFilterDto;
import com.libreria.dto.ReporteIngresosDto;
import com.libreria.entity.Ingreso;
import com.libreria.entity.Pais;
import com.libreria.repository.IngresoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngresoService {

    private final IngresoRepository ingresoRepository;

    public List<ReporteIngresosDto> generarReporte(ReporteFilterDto filtro) {
        List<Ingreso> ingresos = ingresoRepository.buscarPorFiltros(
                filtro.getFechaInicio(), filtro.getFechaFin(), filtro.getPaisId());

        Map<Long, List<Ingreso>> ingresosPorPais = ingresos.stream()
                .collect(Collectors.groupingBy(i -> i.getPais().getId(), LinkedHashMap::new, Collectors.toList()));

        return ingresosPorPais.values().stream()
                .map(this::toReporteIngresosDto)
                .collect(Collectors.toList());
    }

    private ReporteIngresosDto toReporteIngresosDto(List<Ingreso> ingresosDelPais) {
        Pais pais = ingresosDelPais.get(0).getPais();

        BigDecimal totalGeneral = ingresosDelPais.stream()
                .map(Ingreso::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<IngresoResponseDto> ingresoDtos = ingresosDelPais.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return new ReporteIngresosDto(pais.getNombre(), pais.getMonedaCodigo(), ingresoDtos, totalGeneral);
    }

    private IngresoResponseDto toResponseDto(Ingreso ingreso) {
        return new IngresoResponseDto(ingreso.getFecha(), ingreso.getNumeroFactura(),
                ingreso.getImporteLibro(), ingreso.getGastoEnvio(), ingreso.getTotal());
    }
}
