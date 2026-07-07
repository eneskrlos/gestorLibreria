package com.libreria.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.libreria.entity.Ingreso;
import com.libreria.entity.Licencia;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoLicencia;
import com.libreria.repository.LicenciaRepository;
import com.libreria.repository.PaisRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IngresoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void activarLicenciaDePrueba() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-INGRESOS", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));
    }

    private Pais buscarPaisPorNombre(String nombre) {
        return paisRepository.findAll().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void obtenerReporte_conIngresosEnElRango_devuelveAgrupadoPorPaisConTotalGeneral() throws Exception {
        Pais argentina = buscarPaisPorNombre("Argentina");
        entityManager.persist(new Ingreso(8001L, LocalDate.of(2026, 5, 10), "F-8001",
                new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("110.00"), argentina));
        entityManager.persist(new Ingreso(8002L, LocalDate.of(2026, 5, 15), "F-8002",
                new BigDecimal("200.00"), new BigDecimal("20.00"), new BigDecimal("220.00"), argentina));
        entityManager.flush();

        mockMvc.perform(get("/api/ingresos/reporte")
                        .param("fechaInicio", "2026-05-01")
                        .param("fechaFin", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paisNombre").value("Argentina"))
                .andExpect(jsonPath("$[0].monedaCodigo").value("ARS"))
                .andExpect(jsonPath("$[0].ingresos.length()").value(2))
                .andExpect(jsonPath("$[0].totalGeneral").value(330.00));
    }

    @Test
    void obtenerReporte_sinFechaInicio_retorna400() throws Exception {
        mockMvc.perform(get("/api/ingresos/reporte")
                        .param("fechaFin", "2026-05-31"))
                .andExpect(status().isBadRequest());
    }
}
