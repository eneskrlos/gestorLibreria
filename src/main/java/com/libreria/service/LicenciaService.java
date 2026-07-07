package com.libreria.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.libreria.entity.Licencia;
import com.libreria.repository.LicenciaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenciaService {

    private final LicenciaRepository licenciaRepository;

    public boolean validarLicencia() {
        verificarVencimiento();
        return licenciaRepository.findFirstByActivaTrue().isPresent();
    }

    public Licencia activarLicencia(String clave) {
        Licencia licencia = licenciaRepository.findByClave(clave)
                .orElseThrow(() -> new LicenciaNoEncontradaException(
                        "No se encontró ninguna licencia con la clave ingresada."));

        if (licencia.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new LicenciaVencidaException(
                    "La licencia ingresada ya está vencida. Contacte al administrador para renovarla.");
        }

        licencia.setActiva(true);
        return licenciaRepository.save(licencia);
    }

    public void verificarVencimiento() {
        licenciaRepository.findFirstByActivaTrue().ifPresent(licencia -> {
            if (licencia.getFechaVencimiento().isBefore(LocalDate.now())) {
                licencia.setActiva(false);
                licenciaRepository.save(licencia);
            }
        });
    }
}
