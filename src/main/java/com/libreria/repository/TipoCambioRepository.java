package com.libreria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libreria.entity.Pais;
import com.libreria.entity.TipoCambio;

public interface TipoCambioRepository extends JpaRepository<TipoCambio, Long> {

    Optional<TipoCambio> findTopByPaisOrderByFechaConsultaDesc(Pais pais);
}
