package com.libreria.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.service.LicenciaService;

@ExtendWith(MockitoExtension.class)
class LicenciaInterceptorTest {

    @Mock
    private LicenciaService licenciaService;

    private LicenciaInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new LicenciaInterceptor(licenciaService, new ObjectMapper());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandle_conLicenciaVigente_permiteContinuar() throws Exception {
        when(licenciaService.validarLicencia()).thenReturn(true);

        boolean continuar = interceptor.preHandle(request, response, new Object());

        assertThat(continuar).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void preHandle_sinLicenciaVigente_bloqueaCon403YMensajeClaro() throws Exception {
        when(licenciaService.validarLicencia()).thenReturn(false);

        boolean continuar = interceptor.preHandle(request, response, new Object());

        assertThat(continuar).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getContentAsString()).contains("mensaje");
        assertThat(response.getContentAsString()).contains("licencia activa");
    }
}
