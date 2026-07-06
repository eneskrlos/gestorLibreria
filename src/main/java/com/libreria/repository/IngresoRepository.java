package com.libreria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.libreria.entity.Ingreso;

// Extiende Repository (no JpaRepository) para no exponer save/delete: los ingresos son solo lectura.
public interface IngresoRepository extends Repository<Ingreso, Long> {

    @Query("SELECT i FROM Ingreso i "
            + "WHERE i.fecha BETWEEN :fechaInicio AND :fechaFin "
            + "AND (:paisId IS NULL OR i.pais.id = :paisId) "
            + "ORDER BY i.pais.nombre, i.fecha")
    List<Ingreso> buscarPorFiltros(@Param("fechaInicio") LocalDate fechaInicio,
                                    @Param("fechaFin") LocalDate fechaFin,
                                    @Param("paisId") Long paisId);
}
