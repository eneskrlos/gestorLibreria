package com.libreria.config;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.service.LicenciaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LicenciaInterceptor implements HandlerInterceptor {

    private final LicenciaService licenciaService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (licenciaService.validarLicencia()) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> cuerpo = Map.of(
                "error", "Licencia requerida",
                "mensaje", "El sistema no tiene una licencia activa. Por favor, contacte al administrador para activarla.");
        response.getWriter().write(objectMapper.writeValueAsString(cuerpo));
        return false;
    }
}
