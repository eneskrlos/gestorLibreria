package com.libreria.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.dto.EgresoRequestDto;
import com.libreria.entity.Pais;
import com.libreria.repository.PaisRepository;
import com.libreria.service.exchange.ExchangeRateApiClient;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EgresoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaisRepository paisRepository;

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    private Pais buscarPaisPorNombre(String nombre) {
        return paisRepository.findAll().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void registrarEgreso_conDatosValidos_retorna201ConElImporteUsdCalculado() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        when(exchangeRateApiClient.obtenerValorUsd("ARS")).thenReturn(new BigDecimal("0.001100"));

        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.of(2026, 7, 1));
        request.setConcepto("Impuesto de exportación");
        request.setImporteMonedaLocal(new BigDecimal("1000.00"));
        request.setPaisId(argentina.getId());

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importeUsd").value(1.10))
                .andExpect(jsonPath("$.importeMonedaLocal").value(1000.00))
                .andExpect(jsonPath("$.monedaCodigo").value("ARS"))
                .andExpect(jsonPath("$.paisNombre").value("Argentina"))
                .andExpect(jsonPath("$.concepto").value("Impuesto de exportación"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void registrarEgreso_conCamposInvalidos_retorna400() throws Exception {
        EgresoRequestDto request = new EgresoRequestDto();

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarEgreso_conImporteNegativo_retorna400() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");

        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.now());
        request.setConcepto("Arancel aduanero");
        request.setImporteMonedaLocal(new BigDecimal("-10.00"));
        request.setPaisId(argentina.getId());

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
