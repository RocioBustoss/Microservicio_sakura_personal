package com.sakura.personal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.personal.model.ProfesionalServicio;

@Repository
public interface ProfesionalServicioRepository extends JpaRepository<ProfesionalServicio, Long> {

	boolean existsByProfesionalIdAndServicioId(Long profesionalId, Long servicioId);

	List<ProfesionalServicio> findByProfesionalId(Long profesionalId);

	void deleteByProfesionalId(Long profesionalId);

}
