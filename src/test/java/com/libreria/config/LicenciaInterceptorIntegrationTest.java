package com.libreria.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.libreria.entity.Licencia;
import com.libreria.entity.TipoLicencia;
import com.libreria.repository.LicenciaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LicenciaInterceptorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Test
    void sinLicenciaActiva_bloqueaAccesoAEndpointsProtegidosCon403() throws Exception {
        mockMvc.perform(get("/api/paises"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Licencia requerida"))
                .andExpect(jsonPath("$.mensaje").value(
                        "El sistema no tiene una licencia activa. Por favor, contacte al administrador para activarla."));
    }

    @Test
    void conLicenciaVigente_permiteAccesoNormalAEndpointsProtegidos() throws Exception {
        licenciaRepository.save(new Licencia(null, "CLAVE-SISTEMA-VIGENTE", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));

        mockMvc.perform(get("/api/paises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void conLicenciaVencida_bloqueaAccesoYLaDesactivaAutomaticamente() throws Exception {
        Licencia licenciaVencida = licenciaRepository.save(new Licencia(null, "CLAVE-SISTEMA-VENCIDA",
                LocalDate.now().minusMonths(2), LocalDate.now().minusDays(1), TipoLicencia.MENSUAL, true));

        mockMvc.perform(get("/api/paises"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Licencia requerida"));

        boolean sigueActiva = licenciaRepository.findByClave(licenciaVencida.getClave())
                .map(Licencia::getActiva)
                .orElse(true);
        assertThat(sigueActiva).isFalse();
    }
}
