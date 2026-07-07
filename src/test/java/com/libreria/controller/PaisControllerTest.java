package com.libreria.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
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
class PaisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @BeforeEach
    void activarLicenciaDePrueba() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-PAISES", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));
    }

    @Test
    void listarPaises_devuelveLos5PaisesIniciales() throws Exception {
        mockMvc.perform(get("/api/paises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.nombre == 'Argentina')].monedaCodigo").value("ARS"))
                .andExpect(jsonPath("$[?(@.nombre == 'Brasil')].monedaCodigo").value("BRL"))
                .andExpect(jsonPath("$[0].id").exists());
    }
}
