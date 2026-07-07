package com.libreria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libreria.entity.Licencia;
import com.libreria.entity.TipoLicencia;
import com.libreria.repository.LicenciaRepository;

@ExtendWith(MockitoExtension.class)
class LicenciaServiceTest {

    @Mock
    private LicenciaRepository licenciaRepository;

    private LicenciaService licenciaService;

    @BeforeEach
    void setUp() {
        licenciaService = new LicenciaService(licenciaRepository);
    }

    @Test
    void validarLicencia_conLicenciaActivaYVigente_devuelveTrue() {
        Licencia licenciaVigente = new Licencia(1L, "CLAVE-1", LocalDate.now().minusDays(5),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true);
        when(licenciaRepository.findFirstByActivaTrue()).thenReturn(Optional.of(licenciaVigente));

        boolean resultado = licenciaService.validarLicencia();

        assertThat(resultado).isTrue();
        verify(licenciaRepository, never()).save(any(Licencia.class));
    }

    @Test
    void validarLicencia_sinLicenciaActiva_devuelveFalse() {
        when(licenciaRepository.findFirstByActivaTrue()).thenReturn(Optional.empty());

        boolean resultado = licenciaService.validarLicencia();

        assertThat(resultado).isFalse();
    }

    @Test
    void validarLicencia_conLicenciaActivaPeroVencida_laDesactivaYDevuelveFalse() {
        Licencia licenciaVencida = new Licencia(2L, "CLAVE-2", LocalDate.now().minusMonths(2),
                LocalDate.now().minusDays(1), TipoLicencia.MENSUAL, true);
        // Primera consulta (dentro de verificarVencimiento): la encuentra activa (y la desactiva).
        // Segunda consulta (dentro de validarLicencia): en la BD real ya no aparecería como activa.
        when(licenciaRepository.findFirstByActivaTrue())
                .thenReturn(Optional.of(licenciaVencida), Optional.empty());

        boolean resultado = licenciaService.validarLicencia();

        assertThat(resultado).isFalse();
        assertThat(licenciaVencida.getActiva()).isFalse();
        verify(licenciaRepository).save(licenciaVencida);
    }

    @Test
    void activarLicencia_conClaveValidaYVigente_laActivaYPersiste() {
        Licencia licencia = new Licencia(3L, "CLAVE-3", LocalDate.now(),
                LocalDate.now().plusMonths(3), TipoLicencia.TRIMESTRAL, false);
        when(licenciaRepository.findByClave("CLAVE-3")).thenReturn(Optional.of(licencia));
        when(licenciaRepository.save(any(Licencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Licencia resultado = licenciaService.activarLicencia("CLAVE-3");

        assertThat(resultado.getActiva()).isTrue();
        verify(licenciaRepository).save(licencia);
    }

    @Test
    void activarLicencia_conClaveInexistente_lanzaLicenciaNoEncontradaException() {
        when(licenciaRepository.findByClave("CLAVE-INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licenciaService.activarLicencia("CLAVE-INEXISTENTE"))
                .isInstanceOf(LicenciaNoEncontradaException.class);
    }

    @Test
    void activarLicencia_conClaveVencida_lanzaLicenciaVencidaExceptionYNoLaActiva() {
        Licencia licenciaVencida = new Licencia(4L, "CLAVE-4", LocalDate.now().minusMonths(2),
                LocalDate.now().minusDays(1), TipoLicencia.MENSUAL, false);
        when(licenciaRepository.findByClave("CLAVE-4")).thenReturn(Optional.of(licenciaVencida));

        assertThatThrownBy(() -> licenciaService.activarLicencia("CLAVE-4"))
                .isInstanceOf(LicenciaVencidaException.class);

        assertThat(licenciaVencida.getActiva()).isFalse();
        verify(licenciaRepository, never()).save(any(Licencia.class));
    }

    @Test
    void verificarVencimiento_sinLicenciaActiva_noHaceNada() {
        when(licenciaRepository.findFirstByActivaTrue()).thenReturn(Optional.empty());

        licenciaService.verificarVencimiento();

        verify(licenciaRepository, never()).save(any(Licencia.class));
    }
}
