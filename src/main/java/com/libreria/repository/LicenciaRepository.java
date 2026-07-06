package com.libreria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libreria.entity.Licencia;

public interface LicenciaRepository extends JpaRepository<Licencia, Long> {

    Optional<Licencia> findByClave(String clave);

    Optional<Licencia> findFirstByActivaTrue();
}
