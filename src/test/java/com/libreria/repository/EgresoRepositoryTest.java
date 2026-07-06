package com.libreria.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.libreria.entity.Egreso;
import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EgresoRepositoryTest {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private TipoCambioRepository tipoCambioRepository;

    private Egreso crearEgreso(LocalDate fecha, Pais pais, TipoCambio tipoCambio) {
        Egreso egreso = new Egreso(null, fecha, "Impuesto de exportación", new BigDecimal("100.00"),
                new BigDecimal("10.00"), pais, tipoCambio, LocalDateTime.now());
        return egresoRepository.save(egreso);
    }

    @Test
    void buscarPorFiltros_conRangoDeFechasSinPais_devuelveTodosOrdenadosPorPais() {
        Pais paisA = paisRepository.save(new Pais(null, "PaisEgresoA", "PEA", "Moneda A"));
        Pais paisB = paisRepository.save(new Pais(null, "PaisEgresoB", "PEB", "Moneda B"));
        TipoCambio tcA = tipoCambioRepository.save(
                new TipoCambio(null, paisA, new BigDecimal("0.001000"), LocalDateTime.now(), FuenteTipoCambio.API));
        TipoCambio tcB = tipoCambioRepository.save(
                new TipoCambio(null, paisB, new BigDecimal("0.002000"), LocalDateTime.now(), FuenteTipoCambio.API));

        crearEgreso(LocalDate.of(2026, 1, 10), paisA, tcA);
        crearEgreso(LocalDate.of(2026, 1, 15), paisB, tcB);
        crearEgreso(LocalDate.of(2025, 1, 1), paisA, tcA); // fuera de rango

        List<Egreso> resultado = egresoRepository.buscarPorFiltros(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(e -> e.getPais().getNombre())
                .containsExactly("PaisEgresoA", "PaisEgresoB");
    }

    @Test
    void buscarPorFiltros_conPaisEspecifico_devuelveSoloLosDeEsePais() {
        Pais paisA = paisRepository.save(new Pais(null, "PaisEgresoC", "PEC", "Moneda C"));
        Pais paisB = paisRepository.save(new Pais(null, "PaisEgresoD", "PED", "Moneda D"));
        TipoCambio tcA = tipoCambioRepository.save(
                new TipoCambio(null, paisA, new BigDecimal("0.001000"), LocalDateTime.now(), FuenteTipoCambio.API));
        TipoCambio tcB = tipoCambioRepository.save(
                new TipoCambio(null, paisB, new BigDecimal("0.002000"), LocalDateTime.now(), FuenteTipoCambio.API));

        crearEgreso(LocalDate.of(2026, 2, 5), paisA, tcA);
        crearEgreso(LocalDate.of(2026, 2, 6), paisB, tcB);

        List<Egreso> resultado = egresoRepository.buscarPorFiltros(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), paisA.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPais().getNombre()).isEqualTo("PaisEgresoC");
    }

    @Test
    void buscarPorFiltros_sinResultadosEnElRango_devuelveListaVacia() {
        Pais pais = paisRepository.save(new Pais(null, "PaisEgresoE", "PEE", "Moneda E"));
        TipoCambio tc = tipoCambioRepository.save(
                new TipoCambio(null, pais, new BigDecimal("0.001000"), LocalDateTime.now(), FuenteTipoCambio.API));
        crearEgreso(LocalDate.of(2026, 3, 1), pais, tc);

        List<Egreso> resultado = egresoRepository.buscarPorFiltros(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31), null);

        assertThat(resultado).isEmpty();
    }
}
