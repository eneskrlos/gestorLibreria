package com.libreria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.libreria.entity.Egreso;

public interface EgresoRepository extends JpaRepository<Egreso, Long> {

    @Query("SELECT e FROM Egreso e "
            + "WHERE e.fecha BETWEEN :fechaInicio AND :fechaFin "
            + "AND (:paisId IS NULL OR e.pais.id = :paisId) "
            + "ORDER BY e.pais.nombre, e.fecha")
    List<Egreso> buscarPorFiltros(@Param("fechaInicio") LocalDate fechaInicio,
                                   @Param("fechaFin") LocalDate fechaFin,
                                   @Param("paisId") Long paisId);
}
