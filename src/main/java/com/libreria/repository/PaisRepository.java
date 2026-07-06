package com.libreria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libreria.entity.Pais;

public interface PaisRepository extends JpaRepository<Pais, Long> {
}
