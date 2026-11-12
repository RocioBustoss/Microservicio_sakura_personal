package com.sakura.personal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.personal.exception.RecursoNoEncontradoException;
import com.sakura.personal.exception.ReglaNegocioException;
import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.Turno;
import com.sakura.personal.repository.ProfesionalRepository;
import com.sakura.personal.repository.TurnoRepository;

@Service
public class TurnoService {

	@Autowired
	private TurnoRepository turnoRepository;

	@Autowired
	private ProfesionalRepository profesionalRepository;

	public List<Turno> listar(Long profesionalId) {
		return turnoRepository.findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(profesionalId);
	}

	public Turno guardar(Long profesionalId, Turno turno) {
		Profesional profesional = profesionalRepository.findById(profesionalId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el profesional"));
		if (!turno.getHoraInicio().isBefore(turno.getHoraFin())) {
			throw new ReglaNegocioException("La hora de inicio debe ser anterior a la de término");
		}
		validarAlmuerzo(turno);
		List<Turno> delDia = turnoRepository.findByProfesionalIdAndDiaSemana(profesionalId, turno.getDiaSemana());
		for (Turno otro : delDia) {
			if (seSolapan(turno, otro)) {
				throw new ReglaNegocioException("El turno se solapa con otro horario del profesional");
			}
		}
		turno.setId(null);
		turno.setProfesional(profesional);
		return turnoRepository.save(turno);
	}

	private boolean seSolapan(Turno turno, Turno otro) {
		return turno.getHoraInicio().isBefore(otro.getHoraFin()) && otro.getHoraInicio().isBefore(turno.getHoraFin());
	}

	private void validarAlmuerzo(Turno turno) {
		if (turno.getHoraAlmuerzoInicio() == null && turno.getHoraAlmuerzoFin() == null) {
			return;
		}
		if (turno.getHoraAlmuerzoInicio() == null || turno.getHoraAlmuerzoFin() == null
				|| !turno.getHoraAlmuerzoInicio().isBefore(turno.getHoraAlmuerzoFin())
				|| turno.getHoraAlmuerzoInicio().isBefore(turno.getHoraInicio())
				|| turno.getHoraAlmuerzoFin().isAfter(turno.getHoraFin())) {
			throw new ReglaNegocioException("El almuerzo debe quedar dentro del turno");
		}
	}

}
