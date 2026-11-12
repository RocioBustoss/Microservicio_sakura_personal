package com.sakura.personal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.personal.client.AgendaCliente;
import com.sakura.personal.client.CatalogoCliente;
import com.sakura.personal.client.ReservaDTO;
import com.sakura.personal.client.ServicioDTO;
import com.sakura.personal.exception.RecursoNoEncontradoException;
import com.sakura.personal.exception.ReglaNegocioException;
import com.sakura.personal.model.Ausencia;
import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.ProfesionalServicio;
import com.sakura.personal.model.Turno;
import com.sakura.personal.repository.AusenciaRepository;
import com.sakura.personal.repository.ProfesionalRepository;
import com.sakura.personal.repository.ProfesionalServicioRepository;
import com.sakura.personal.repository.TurnoRepository;

@Service
public class ProfesionalService {

	@Autowired
	private ProfesionalRepository profesionalRepository;

	@Autowired
	private ProfesionalServicioRepository profesionalServicioRepository;

	@Autowired
	private AusenciaRepository ausenciaRepository;

	@Autowired
	private CatalogoCliente catalogoCliente;

	@Autowired
	private AgendaCliente agendaCliente;

	@Autowired
	private TurnoService turnoService;

	@Autowired
	private TurnoRepository turnoRepository;

	public List<Profesional> listarActivos() {
		return profesionalRepository.findByActivoTrueOrderByNombreAsc();
	}

	public List<Profesional> listarTodos() {
		return profesionalRepository.findAllByOrderByNombreAsc();
	}

	public Profesional buscar(Long id) {
		return profesionalRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el profesional"));
	}

	public Profesional crear(Profesional datos) {
		datos.setId(null);
		datos.setActivo(true);
		return profesionalRepository.save(datos);
	}

	public Profesional actualizar(Long id, Profesional datos) {
		Profesional actual = buscar(id);
		actual.setNombre(datos.getNombre());
		actual.setCorreo(datos.getCorreo());
		actual.setTelefono(datos.getTelefono());
		actual.setEspecialidad(datos.getEspecialidad());
		actual.setActivo(datos.isActivo());
		return profesionalRepository.save(actual);
	}

	public void eliminar(Long id, String authorization) {
		buscar(id);
		if (agendaCliente.tieneReservas(id, authorization)) {
			throw new ReglaNegocioException(
					"No se puede eliminar: tiene citas registradas. Cancele o reasigne esas citas primero");
		}
		profesionalServicioRepository.deleteByProfesionalId(id);
		turnoRepository.deleteByProfesionalId(id);
		ausenciaRepository.deleteByProfesionalId(id);
		profesionalRepository.deleteById(id);
	}

	public List<Turno> listarTurnos(Long profesionalId) {
		buscar(profesionalId);
		return turnoService.listar(profesionalId);
	}

	public Turno agregarTurno(Long profesionalId, Turno turno) {
		return turnoService.guardar(profesionalId, turno);
	}

	public List<ServicioDTO> listarServicios(Long profesionalId) {
		buscar(profesionalId);
		List<ServicioDTO> servicios = new ArrayList<>();
		for (ProfesionalServicio vinculo : profesionalServicioRepository.findByProfesionalId(profesionalId)) {
			servicios.add(catalogoCliente.obtener(vinculo.getServicioId()));
		}
		return servicios;
	}

	public List<ServicioDTO> asociarServicios(Long profesionalId, List<Long> servicioIds) {
		Profesional profesional = buscar(profesionalId);
		profesionalServicioRepository.deleteByProfesionalId(profesionalId);
		if (servicioIds != null) {
			for (Long servicioId : servicioIds) {
				catalogoCliente.obtener(servicioId);
				ProfesionalServicio vinculo = new ProfesionalServicio();
				vinculo.setProfesional(profesional);
				vinculo.setServicioId(servicioId);
				profesionalServicioRepository.save(vinculo);
			}
		}
		return listarServicios(profesionalId);
	}

	public Ausencia registrarAusencia(Long profesionalId, Ausencia ausencia) {
		Profesional profesional = buscar(profesionalId);
		if (ausencia.getFechaFin().isBefore(ausencia.getFechaInicio())) {
			throw new ReglaNegocioException("La fecha de término no puede ser anterior a la de inicio");
		}
		ausencia.setId(null);
		ausencia.setProfesional(profesional);
		return ausenciaRepository.save(ausencia);
	}

	public List<Ausencia> listarAusencias(Long profesionalId) {
		buscar(profesionalId);
		return ausenciaRepository.findByProfesionalIdOrderByFechaInicioAsc(profesionalId);
	}

	public List<ReservaDTO> historial(Long profesionalId, LocalDate desde, LocalDate hasta, String authorization) {
		buscar(profesionalId);
		return agendaCliente.historial(profesionalId, desde, hasta, authorization);
	}

}
