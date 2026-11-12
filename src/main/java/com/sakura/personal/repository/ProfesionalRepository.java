package com.sakura.personal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.personal.model.Profesional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

	List<Profesional> findByActivoTrueOrderByNombreAsc();

	List<Profesional> findAllByOrderByNombreAsc();

}
