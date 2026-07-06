package com.libreria.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.libreria.entity.Licencia;
import com.libreria.entity.TipoLicencia;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LicenciaRepositoryTest {

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Test
    void findByClave_conClaveExistente_devuelveLaLicencia() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-001", LocalDate.now(),
                LocalDate.now().plusMonths(1), TipoLicencia.MENSUAL, true));

        Optional<Licencia> encontrada = licenciaRepository.findByClave("CLAVE-TEST-001");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTipo()).isEqualTo(TipoLicencia.MENSUAL);
    }

    @Test
    void findByClave_conClaveInexistente_devuelveVacio() {
        Optional<Licencia> encontrada = licenciaRepository.findByClave("CLAVE-QUE-NO-EXISTE");

        assertThat(encontrada).isEmpty();
    }

    @Test
    void findFirstByActivaTrue_conLicenciaActiva_devuelveLaLicenciaActiva() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-INACTIVA", LocalDate.now().minusMonths(2),
                LocalDate.now().minusMonths(1), TipoLicencia.MENSUAL, false));
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-ACTIVA", LocalDate.now(),
                LocalDate.now().plusMonths(3), TipoLicencia.TRIMESTRAL, true));

        Optional<Licencia> activa = licenciaRepository.findFirstByActivaTrue();

        assertThat(activa).isPresent();
        assertThat(activa.get().getClave()).isEqualTo("CLAVE-TEST-ACTIVA");
    }

    @Test
    void findFirstByActivaTrue_sinLicenciasActivas_devuelveVacio() {
        licenciaRepository.save(new Licencia(null, "CLAVE-TEST-VENCIDA", LocalDate.now().minusMonths(2),
                LocalDate.now().minusMonths(1), TipoLicencia.MENSUAL, false));

        Optional<Licencia> activa = licenciaRepository.findFirstByActivaTrue();

        assertThat(activa).isEmpty();
    }
}
