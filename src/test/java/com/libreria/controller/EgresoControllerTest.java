package com.libreria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
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
import com.libreria.entity.Egreso;
import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Licencia;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;
import com.libreria.entity.TipoLicencia;
import com.libreria.repository.EgresoRepository;
import com.libreria.repository.LicenciaRepository;
import com.libreria.repository.PaisRepository;
import com.libreria.repository.TipoCambioRepository;
import com.libreria.service.exchange.ExchangeRateApiClient;
import com.libreria.service.exchange.ExchangeRateApiException;

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

    @Autowired
    private TipoCambioRepository tipoCambioRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    @BeforeEach
    void activarLicenciaDePrueba() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-EGRESOS", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));
    }

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.mensaje").exists());
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void registrarEgreso_conPaisInexistente_retorna404() throws Exception {
        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.now());
        request.setConcepto("Arancel aduanero");
        request.setImporteMonedaLocal(new BigDecimal("500.00"));
        request.setPaisId(999L);

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.mensaje").value("No se encontró el país seleccionado."));
    }

    @Test
    void registrarEgreso_conApiCaidaYTipoCambioPrevioEnBd_usaElUltimoValorComoFallback() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        tipoCambioRepository.save(new TipoCambio(null, argentina, new BigDecimal("0.001000"),
                LocalDateTime.now().minusDays(1), FuenteTipoCambio.API));
        when(exchangeRateApiClient.obtenerValorUsd("ARS"))
                .thenThrow(new ExchangeRateApiException("timeout"));

        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.now());
        request.setConcepto("Arancel aduanero");
        request.setImporteMonedaLocal(new BigDecimal("1000.00"));
        request.setPaisId(argentina.getId());

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importeUsd").value(1.00))
                .andExpect(jsonPath("$.importeMonedaLocal").value(1000.00));

        boolean existeTipoCambioManual = tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(argentina)
                .map(tc -> tc.getFuente() == FuenteTipoCambio.MANUAL)
                .orElse(false);
        assertThat(existeTipoCambioManual).isTrue();
    }

    @Test
    void registrarEgreso_conApiCaidaYSinTipoCambioPrevio_retorna503() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        when(exchangeRateApiClient.obtenerValorUsd("ARS"))
                .thenThrow(new ExchangeRateApiException("timeout"));

        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.now());
        request.setConcepto("Arancel aduanero");
        request.setImporteMonedaLocal(new BigDecimal("500.00"));
        request.setPaisId(argentina.getId());

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Servicio no disponible"))
                .andExpect(jsonPath("$.mensaje").value(
                        "No se pudo obtener el tipo de cambio actualizado para Argentina. Intente nuevamente más tarde."));
    }

    @Test
    void flujoCompleto_altaConversionYReporte_reflejaElEgresoRegistradoConImporteUsdCalculado() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        when(exchangeRateApiClient.obtenerValorUsd("ARS")).thenReturn(new BigDecimal("0.001100"));

        EgresoRequestDto request = new EgresoRequestDto();
        request.setFecha(LocalDate.of(2026, 6, 15));
        request.setConcepto("Impuesto de exportación");
        request.setImporteMonedaLocal(new BigDecimal("2000.00"));
        request.setPaisId(argentina.getId());

        mockMvc.perform(post("/api/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importeUsd").value(2.20));

        mockMvc.perform(get("/api/egresos/reporte")
                        .param("fechaInicio", "2026-06-01")
                        .param("fechaFin", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paisNombre").value("Argentina"))
                .andExpect(jsonPath("$[0].monedaCodigo").value("ARS"))
                .andExpect(jsonPath("$[0].egresos.length()").value(1))
                .andExpect(jsonPath("$[0].egresos[0].concepto").value("Impuesto de exportación"))
                .andExpect(jsonPath("$[0].egresos[0].importeMonedaLocal").value(2000.00))
                .andExpect(jsonPath("$[0].egresos[0].importeUsd").value(2.20))
                .andExpect(jsonPath("$[0].totalMonedaLocal").value(2000.00))
                .andExpect(jsonPath("$[0].totalUsd").value(2.20));
    }

    @Test
    void obtenerReporte_conEgresosEnElRango_devuelveAgrupadoPorPaisConSubtotales() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        TipoCambio tipoCambio = tipoCambioRepository.save(new TipoCambio(null, argentina,
                new BigDecimal("0.001100"), LocalDateTime.now(), FuenteTipoCambio.API));
        egresoRepository.save(new Egreso(null, LocalDate.of(2026, 5, 10), "Impuesto",
                new BigDecimal("1000.00"), new BigDecimal("1.10"), argentina, tipoCambio, LocalDateTime.now()));
        egresoRepository.save(new Egreso(null, LocalDate.of(2026, 5, 15), "Arancel",
                new BigDecimal("500.00"), new BigDecimal("0.55"), argentina, tipoCambio, LocalDateTime.now()));

        mockMvc.perform(get("/api/egresos/reporte")
                        .param("fechaInicio", "2026-05-01")
                        .param("fechaFin", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paisNombre").value("Argentina"))
                .andExpect(jsonPath("$[0].monedaCodigo").value("ARS"))
                .andExpect(jsonPath("$[0].egresos.length()").value(2))
                .andExpect(jsonPath("$[0].totalMonedaLocal").value(1500.00))
                .andExpect(jsonPath("$[0].totalUsd").value(1.65));
    }

    @Test
    void obtenerReporte_sinFechaInicio_retorna400() throws Exception {
        mockMvc.perform(get("/api/egresos/reporte")
                        .param("fechaFin", "2026-05-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
