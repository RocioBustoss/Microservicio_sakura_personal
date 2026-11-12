package com.sakura.personal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.personal.model.Turno;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

	List<Turno> findByProfesionalIdAndDiaSemana(Long profesionalId, Integer diaSemana);

	List<Turno> findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(Long profesionalId);

	void deleteByProfesionalId(Long profesionalId);

}
