package com.libreria.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.libreria.entity.FuenteTipoCambio;
import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TipoCambioRepositoryTest {

    @Autowired
    private TipoCambioRepository tipoCambioRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Test
    void findTopByPaisOrderByFechaConsultaDesc_devuelveElMasReciente() {
        Pais pais = paisRepository.save(new Pais(null, "PaisDePruebaTipoCambio", "PTC", "Moneda de prueba"));

        TipoCambio antiguo = new TipoCambio(null, pais, new BigDecimal("0.001000"),
                LocalDateTime.now().minusDays(2), FuenteTipoCambio.API);
        TipoCambio reciente = new TipoCambio(null, pais, new BigDecimal("0.001200"),
                LocalDateTime.now(), FuenteTipoCambio.MANUAL);
        tipoCambioRepository.save(antiguo);
        tipoCambioRepository.save(reciente);

        Optional<TipoCambio> resultado = tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(pais);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getValorUsd()).isEqualByComparingTo("0.001200");
        assertThat(resultado.get().getFuente()).isEqualTo(FuenteTipoCambio.MANUAL);
    }

    @Test
    void findTopByPaisOrderByFechaConsultaDesc_sinRegistros_devuelveVacio() {
        Pais pais = paisRepository.save(new Pais(null, "PaisSinTipoCambio", "PSC", "Moneda de prueba"));

        Optional<TipoCambio> resultado = tipoCambioRepository.findTopByPaisOrderByFechaConsultaDesc(pais);

        assertThat(resultado).isEmpty();
    }
}
