package com.libreria.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.dto.LicenciaActivarRequestDto;
import com.libreria.entity.Licencia;
import com.libreria.entity.TipoLicencia;
import com.libreria.repository.LicenciaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LicenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Test
    void obtenerEstado_sinLicenciaActiva_devuelveVigenteFalse() throws Exception {
        mockMvc.perform(get("/api/licencia/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(false));
    }

    @Test
    void obtenerEstado_conLicenciaActivaYVigente_devuelveVigenteTrue() throws Exception {
        licenciaRepository.save(new Licencia(null, "CLAVE-ESTADO-OK", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));

        mockMvc.perform(get("/api/licencia/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(true));
    }

    @Test
    void activarLicencia_conClaveValidaYVigente_retorna200ConLicenciaActivada() throws Exception {
        licenciaRepository.save(new Licencia(null, "CLAVE-ACTIVAR-OK", LocalDate.now(),
                LocalDate.now().plusMonths(3), TipoLicencia.TRIMESTRAL, false));

        LicenciaActivarRequestDto request = new LicenciaActivarRequestDto();
        request.setClave("CLAVE-ACTIVAR-OK");

        mockMvc.perform(post("/api/licencia/activar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value("CLAVE-ACTIVAR-OK"))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.tipo").value("TRIMESTRAL"));
    }

    @Test
    void activarLicencia_conClaveVacia_retorna400() throws Exception {
        LicenciaActivarRequestDto request = new LicenciaActivarRequestDto();

        mockMvc.perform(post("/api/licencia/activar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
