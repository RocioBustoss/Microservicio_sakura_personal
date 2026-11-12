package com.sakura.personal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sakura.personal.client.ReservaDTO;
import com.sakura.personal.client.ServicioDTO;
import com.sakura.personal.client.SesionGuard;
import com.sakura.personal.exception.RecursoNoEncontradoException;
import com.sakura.personal.exception.ReglaNegocioException;
import com.sakura.personal.model.Ausencia;
import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.Turno;
import com.sakura.personal.service.ProfesionalService;

@WebMvcTest(ProfesionalController.class)
@ActiveProfiles("test")
class ProfesionalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProfesionalService profesionalService;

	@MockitoBean
	private SesionGuard sesionGuard;

	private static final String AUTH = "Bearer token-admin";
	private static final String CUERPO = "{\"nombre\":\"Ana\",\"correo\":\"ana@test.com\",\"telefono\":\"123\",\"especialidad\":\"Color\",\"activo\":true}";

	@Test
	void listar_Exito() throws Exception {
		when(profesionalService.listarActivos()).thenReturn(List.of(profesional(1L)));

		mockMvc.perform(get("/api/v1/profesionales"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Ana"));
	}

	@Test
	void gestion_Exito() throws Exception {
		when(profesionalService.listarTodos()).thenReturn(List.of(profesional(1L)));

		mockMvc.perform(get("/api/v1/profesionales/gestion").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].especialidad").value("Color"));
	}

	@Test
	void obtener_Exito() throws Exception {
		when(profesionalService.buscar(1L)).thenReturn(profesional(1L));

		mockMvc.perform(get("/api/v1/profesionales/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correo").value("ana@test.com"));
	}

	@Test
	void obtener_NoExiste_Responde404() throws Exception {
		when(profesionalService.buscar(99L)).thenThrow(new RecursoNoEncontradoException("No existe el profesional"));

		mockMvc.perform(get("/api/v1/profesionales/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void crear_Exito() throws Exception {
		when(profesionalService.crear(any(Profesional.class))).thenReturn(profesional(2L));

		mockMvc.perform(post("/api/v1/profesionales")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(2));
	}

	@Test
	void crear_CuerpoVacio_Responde400() throws Exception {
		mockMvc.perform(post("/api/v1/profesionales")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizar_Exito() throws Exception {
		when(profesionalService.actualizar(eq(1L), any(Profesional.class))).thenReturn(profesional(1L));

		mockMvc.perform(put("/api/v1/profesionales/1")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana"));
	}

	@Test
	void eliminar_Exito() throws Exception {
		doNothing().when(profesionalService).eliminar(1L, AUTH);

		mockMvc.perform(delete("/api/v1/profesionales/1").header("Authorization", AUTH))
				.andExpect(status().isNoContent());
	}

	@Test
	void eliminar_ConReservas_Responde409() throws Exception {
		doThrow(new ReglaNegocioException("No se puede eliminar: tiene citas registradas. Cancele o reasigne esas citas primero"))
				.when(profesionalService).eliminar(1L, AUTH);

		mockMvc.perform(delete("/api/v1/profesionales/1").header("Authorization", AUTH))
				.andExpect(status().isConflict());
	}

	@Test
	void turnos_Exito() throws Exception {
		Turno turno = new Turno();
		turno.setDiaSemana(1);
		turno.setHoraInicio(LocalTime.of(9, 0));
		when(profesionalService.listarTurnos(1L)).thenReturn(List.of(turno));

		mockMvc.perform(get("/api/v1/profesionales/1/turnos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].diaSemana").value(1));
	}

	@Test
	void agregarTurno_Exito() throws Exception {
		Turno turno = new Turno();
		turno.setId(5L);
		turno.setDiaSemana(1);
		when(profesionalService.agregarTurno(eq(1L), any(Turno.class))).thenReturn(turno);

		mockMvc.perform(post("/api/v1/profesionales/1/turnos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"diaSemana\":1,\"horaInicio\":\"09:00:00\",\"horaFin\":\"18:00:00\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5));
	}

	@Test
	void servicios_Exito() throws Exception {
		ServicioDTO servicio = new ServicioDTO();
		servicio.setId(10L);
		servicio.setNombre("Corte");
		when(profesionalService.listarServicios(1L)).thenReturn(List.of(servicio));

		mockMvc.perform(get("/api/v1/profesionales/1/servicios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Corte"));
	}

	@Test
	void asociarServicios_Exito() throws Exception {
		ServicioDTO servicio = new ServicioDTO();
		servicio.setId(10L);
		when(profesionalService.asociarServicios(eq(1L), any())).thenReturn(List.of(servicio));

		mockMvc.perform(put("/api/v1/profesionales/1/servicios")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("[10,11]"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(10));
	}

	@Test
	void ausencias_Exito() throws Exception {
		Ausencia ausencia = new Ausencia();
		ausencia.setId(3L);
		ausencia.setMotivo("Vacaciones");
		when(profesionalService.listarAusencias(1L)).thenReturn(List.of(ausencia));

		mockMvc.perform(get("/api/v1/profesionales/1/ausencias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].motivo").value("Vacaciones"));
	}

	@Test
	void registrarAusencia_Exito() throws Exception {
		Ausencia ausencia = new Ausencia();
		ausencia.setId(4L);
		when(profesionalService.registrarAusencia(eq(1L), any(Ausencia.class))).thenReturn(ausencia);

		mockMvc.perform(post("/api/v1/profesionales/1/ausencias")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"fechaInicio\":\"2026-05-01\",\"fechaFin\":\"2026-05-03\",\"motivo\":\"Licencia\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(4));
	}

	@Test
	void historial_Exito() throws Exception {
		ReservaDTO reserva = new ReservaDTO();
		reserva.setId(8L);
		reserva.setNombreServicio("Corte");
		reserva.setFecha(LocalDate.of(2026, 5, 10));
		when(profesionalService.historial(1L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), AUTH))
				.thenReturn(List.of(reserva));

		mockMvc.perform(get("/api/v1/profesionales/1/historial")
				.header("Authorization", AUTH)
				.param("desde", "2026-05-01")
				.param("hasta", "2026-05-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombreServicio").value("Corte"));
	}

	private Profesional profesional(Long id) {
		Profesional profesional = new Profesional();
		profesional.setId(id);
		profesional.setNombre("Ana");
		profesional.setCorreo("ana@test.com");
		profesional.setTelefono("123");
		profesional.setEspecialidad("Color");
		profesional.setActivo(true);
		return profesional;
	}
}
