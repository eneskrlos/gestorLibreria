package com.libreria.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.libreria.entity.Ingreso;
import com.libreria.entity.Pais;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IngresoRepositoryTest {

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Ingreso crearIngreso(long id, LocalDate fecha, Pais pais) {
        Ingreso ingreso = new Ingreso(id, fecha, "F-" + id, new BigDecimal("50.00"),
                new BigDecimal("5.00"), new BigDecimal("55.00"), pais);
        return entityManager.persistAndFlush(ingreso);
    }

    @Test
    void buscarPorFiltros_conRangoDeFechasSinPais_devuelveTodosOrdenadosPorPais() {
        Pais paisA = entityManager.persistAndFlush(new Pais(null, "PaisIngresoA", "PIA", "Moneda A"));
        Pais paisB = entityManager.persistAndFlush(new Pais(null, "PaisIngresoB", "PIB", "Moneda B"));

        crearIngreso(9001L, LocalDate.of(2026, 1, 10), paisA);
        crearIngreso(9002L, LocalDate.of(2026, 1, 15), paisB);
        crearIngreso(9003L, LocalDate.of(2025, 1, 1), paisA); // fuera de rango

        List<Ingreso> resultado = ingresoRepository.buscarPorFiltros(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(i -> i.getPais().getNombre())
                .containsExactly("PaisIngresoA", "PaisIngresoB");
    }

    @Test
    void buscarPorFiltros_conPaisEspecifico_devuelveSoloLosDeEsePais() {
        Pais paisA = entityManager.persistAndFlush(new Pais(null, "PaisIngresoC", "PIC", "Moneda C"));
        Pais paisB = entityManager.persistAndFlush(new Pais(null, "PaisIngresoD", "PID", "Moneda D"));

        crearIngreso(9004L, LocalDate.of(2026, 2, 5), paisA);
        crearIngreso(9005L, LocalDate.of(2026, 2, 6), paisB);

        List<Ingreso> resultado = ingresoRepository.buscarPorFiltros(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), paisA.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPais().getNombre()).isEqualTo("PaisIngresoC");
    }

    @Test
    void buscarPorFiltros_sinResultadosEnElRango_devuelveListaVacia() {
        Pais pais = entityManager.persistAndFlush(new Pais(null, "PaisIngresoE", "PIE", "Moneda E"));
        crearIngreso(9006L, LocalDate.of(2026, 3, 1), pais);

        List<Ingreso> resultado = ingresoRepository.buscarPorFiltros(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31), null);

        assertThat(resultado).isEmpty();
    }
}
