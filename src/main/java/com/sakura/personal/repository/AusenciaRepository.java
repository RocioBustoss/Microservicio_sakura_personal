package com.sakura.personal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sakura.personal.model.Ausencia;

@Repository
public interface AusenciaRepository extends JpaRepository<Ausencia, Long> {

	List<Ausencia> findByProfesionalIdOrderByFechaInicioAsc(Long profesionalId);

	void deleteByProfesionalId(Long profesionalId);

	@Query("""
			select a from Ausencia a
			where a.profesional.id = :profesionalId
			  and a.fechaInicio <= :fecha
			  and a.fechaFin >= :fecha
			""")
	List<Ausencia> buscarEnFecha(@Param("profesionalId") Long profesionalId, @Param("fecha") LocalDate fecha);

}
