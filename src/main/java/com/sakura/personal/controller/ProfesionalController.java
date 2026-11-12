package com.sakura.personal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.personal.client.ReservaDTO;
import com.sakura.personal.client.ServicioDTO;
import com.sakura.personal.client.SesionGuard;
import com.sakura.personal.model.Ausencia;
import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.Turno;
import com.sakura.personal.service.ProfesionalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/profesionales")
public class ProfesionalController {

	@Autowired
	private ProfesionalService profesionalService;

	@Autowired
	private SesionGuard sesionGuard;

	@GetMapping
	public List<Profesional> listar() {
		return profesionalService.listarActivos();
	}

	@GetMapping("/gestion")
	public List<Profesional> gestion(@RequestHeader(value = "Authorization", required = false) String authorization) {
		sesionGuard.exigirAdmin(authorization);
		return profesionalService.listarTodos();
	}

	@GetMapping("/{id}")
	public Profesional obtener(@PathVariable Long id) {
		return profesionalService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<Profesional> crear(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody Profesional profesional) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(profesionalService.crear(profesional), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public Profesional actualizar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@Valid @RequestBody Profesional profesional) {
		sesionGuard.exigirAdmin(authorization);
		return profesionalService.actualizar(id, profesional);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		profesionalService.eliminar(id, authorization);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/turnos")
	public List<Turno> turnos(@PathVariable Long id) {
		return profesionalService.listarTurnos(id);
	}

	@PostMapping("/{id}/turnos")
	public ResponseEntity<Turno> agregarTurno(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@Valid @RequestBody Turno turno) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(profesionalService.agregarTurno(id, turno), HttpStatus.CREATED);
	}

	@GetMapping("/{id}/servicios")
	public List<ServicioDTO> servicios(@PathVariable Long id) {
		return profesionalService.listarServicios(id);
	}

	@PutMapping("/{id}/servicios")
	public List<ServicioDTO> asociarServicios(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@RequestBody List<Long> servicioIds) {
		sesionGuard.exigirAdmin(authorization);
		return profesionalService.asociarServicios(id, servicioIds);
	}

	@GetMapping("/{id}/ausencias")
	public List<Ausencia> ausencias(@PathVariable Long id) {
		return profesionalService.listarAusencias(id);
	}

	@PostMapping("/{id}/ausencias")
	public ResponseEntity<Ausencia> ausencia(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@Valid @RequestBody Ausencia ausencia) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(profesionalService.registrarAusencia(id, ausencia), HttpStatus.CREATED);
	}

	@GetMapping("/{id}/historial")
	public List<ReservaDTO> historial(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		sesionGuard.exigirAdmin(authorization);
		return profesionalService.historial(id, desde, hasta, authorization);
	}

}
