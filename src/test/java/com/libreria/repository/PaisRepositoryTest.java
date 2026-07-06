package com.libreria.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.libreria.entity.Pais;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaisRepositoryTest {

    @Autowired
    private PaisRepository paisRepository;

    @Test
    void findAll_devuelveLosPaisesGuardados() {
        Pais pais = paisRepository.save(new Pais(null, "PaisDePruebaFindAll", "PDA", "Moneda de prueba"));

        List<Pais> paises = paisRepository.findAll();

        assertThat(paises).extracting(Pais::getId).contains(pais.getId());
    }

    @Test
    void findById_devuelvePaisConSusDatosCorrectos() {
        Pais guardado = paisRepository.save(new Pais(null, "PaisDePruebaFindById", "PDB", "Moneda de prueba 2"));

        Optional<Pais> encontrado = paisRepository.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMonedaCodigo()).isEqualTo("PDB");
        assertThat(encontrado.get().getMonedaNombre()).isEqualTo("Moneda de prueba 2");
    }

    @Test
    void findById_conIdInexistente_devuelveVacio() {
        Optional<Pais> encontrado = paisRepository.findById(-1L);

        assertThat(encontrado).isEmpty();
    }
}
