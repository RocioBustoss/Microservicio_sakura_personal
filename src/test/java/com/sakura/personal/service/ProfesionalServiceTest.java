package com.sakura.personal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ProfesionalServiceTest {

	@Mock
	private ProfesionalRepository profesionalRepository;

	@Mock
	private ProfesionalServicioRepository profesionalServicioRepository;

	@Mock
	private AusenciaRepository ausenciaRepository;

	@Mock
	private CatalogoCliente catalogoCliente;

	@Mock
	private AgendaCliente agendaCliente;

	@Mock
	private TurnoService turnoService;

	@Mock
	private TurnoRepository turnoRepository;

	@InjectMocks
	private ProfesionalService profesionalService;

	private Profesional profesionalBase;

	@BeforeEach
	void setUp() {
		profesionalBase = new Profesional(1L, "Sakura Haruno", "sakura@konoha.com", "+56912345678", "Medicina General", true);
	}

	@Nested
	@DisplayName("Pruebas de Búsqueda y Lectura de Profesionales")
	class ConsultasProfesionalesTests {

		@Test
		@DisplayName("listarActivos debe retornar solo profesionales activos ordenados")
		void listarActivos_DebeRetornarLista() {
			when(profesionalRepository.findByActivoTrueOrderByNombreAsc())
					.thenReturn(List.of(profesionalBase));

			List<Profesional> resultado = profesionalService.listarActivos();

			assertThat(resultado).hasSize(1);
			assertThat(resultado.get(0).getNombre()).isEqualTo("Sakura Haruno");
			verify(profesionalRepository).findByActivoTrueOrderByNombreAsc();
		}

		@Test
		@DisplayName("listarTodos debe retornar todos los profesionales")
		void listarTodos_DebeRetornarLista() {
			when(profesionalRepository.findAllByOrderByNombreAsc())
					.thenReturn(List.of(profesionalBase));

			List<Profesional> resultado = profesionalService.listarTodos();

			assertThat(resultado).hasSize(1);
			verify(profesionalRepository).findAllByOrderByNombreAsc();
		}

		@Test
		@DisplayName("buscar por ID existente debe retornar el profesional")
		void buscar_Existente_DebeRetornarProfesional() {
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));

			Profesional resultado = profesionalService.buscar(1L);

			assertThat(resultado).isNotNull();
			assertThat(resultado.getId()).isEqualTo(1L);
		}

		@Test
		@DisplayName("buscar por ID inexistente debe lanzar RecursoNoEncontradoException")
		void buscar_Inexistente_DebeLanzarExcepcion() {
			when(profesionalRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> profesionalService.buscar(99L))
					.isInstanceOf(RecursoNoEncontradoException.class)
					.hasMessage("No existe el profesional");
		}
	}

	@Nested
	@DisplayName("Pruebas de Creación y Actualización")
	class EscrituraProfesionalesTests {

		@Test
		@DisplayName("crear debe forzar id null, activo true y guardar")
		void crear_DebeGuardarProfesionalConActivoTrue() {
			Profesional input = new Profesional(100L, "Tsunade Senju", "tsunade@konoha.com", "+56999999999", "Sannin", false);
			Profesional guardado = new Profesional(1L, "Tsunade Senju", "tsunade@konoha.com", "+56999999999", "Sannin", true);

			when(profesionalRepository.save(any(Profesional.class))).thenReturn(guardado);

			Profesional resultado = profesionalService.crear(input);

			assertThat(input.getId()).isNull();
			assertThat(input.isActivo()).isTrue();
			assertThat(resultado.getId()).isEqualTo(1L);
			verify(profesionalRepository).save(input);
		}

		@Test
		@DisplayName("actualizar debe actualizar datos y guardar cuando existe el profesional")
		void actualizar_Existente_DebeModificarYGuardar() {
			Profesional datosNuevos = new Profesional(null, "Sakura Uchiha", "sakura.u@konoha.com", "+56988888888", "Ninja Médico", false);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(profesionalRepository.save(any(Profesional.class))).thenAnswer(invocation -> invocation.getArgument(0));

			Profesional resultado = profesionalService.actualizar(1L, datosNuevos);

			assertThat(resultado.getNombre()).isEqualTo("Sakura Uchiha");
			assertThat(resultado.getCorreo()).isEqualTo("sakura.u@konoha.com");
			assertThat(resultado.getTelefono()).isEqualTo("+56988888888");
			assertThat(resultado.getEspecialidad()).isEqualTo("Ninja Médico");
			assertThat(resultado.isActivo()).isFalse();
			verify(profesionalRepository).save(profesionalBase);
		}
	}

	@Nested
	@DisplayName("Pruebas de Eliminación")
	class EliminacionTests {

		@Test
		@DisplayName("eliminar debe borrar registros en cascada cuando no tiene reservas")
		void eliminar_SinReservas_DebeEliminarSatisfactoriamente() {
			String token = "Bearer token_abc";
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(agendaCliente.tieneReservas(1L, token)).thenReturn(false);

			profesionalService.eliminar(1L, token);

			verify(profesionalServicioRepository).deleteByProfesionalId(1L);
			verify(turnoRepository).deleteByProfesionalId(1L);
			verify(ausenciaRepository).deleteByProfesionalId(1L);
			verify(profesionalRepository).deleteById(1L);
		}

		@Test
		@DisplayName("eliminar debe lanzar ReglaNegocioException si el profesional tiene reservas")
		void eliminar_ConReservas_DebeLanzarExcepcion() {
			String token = "Bearer token_abc";
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(agendaCliente.tieneReservas(1L, token)).thenReturn(true);

			assertThatThrownBy(() -> profesionalService.eliminar(1L, token))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("No se puede eliminar: tiene citas registradas. Cancele o reasigne esas citas primero");

			verify(profesionalServicioRepository, never()).deleteByProfesionalId(any());
			verify(profesionalRepository, never()).deleteById(any());
		}
	}

	@Nested
	@DisplayName("Pruebas de Gestión de Turnos")
	class TurnosTests {

		@Test
		@DisplayName("listarTurnos debe delegar a turnoService si el profesional existe")
		void listarTurnos_ProfesionalExiste_DebeRetornarTurnos() {
			Turno turno = new Turno(10L, profesionalBase, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoService.listar(1L)).thenReturn(List.of(turno));

			List<Turno> resultado = profesionalService.listarTurnos(1L);

			assertThat(resultado).hasSize(1);
			verify(turnoService).listar(1L);
		}

		@Test
		@DisplayName("agregarTurno debe delegar a turnoService")
		void agregarTurno_DebeDelegarATurnoService() {
			Turno nuevoTurno = new Turno(null, null, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);
			Turno turnoGuardado = new Turno(10L, profesionalBase, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);

			when(turnoService.guardar(1L, nuevoTurno)).thenReturn(turnoGuardado);

			Turno resultado = profesionalService.agregarTurno(1L, nuevoTurno);

			assertThat(resultado.getId()).isEqualTo(10L);
			verify(turnoService).guardar(1L, nuevoTurno);
		}
	}

	@Nested
	@DisplayName("Pruebas de Asociación e Integración con Catálogo de Servicios")
	class ServiciosTests {

		@Test
		@DisplayName("listarServicios debe obtener los DTOs desde el cliente de catálogo")
		void listarServicios_DebeRetornarListaServicioDTO() {
			ProfesionalServicio ps = new ProfesionalServicio(100L, profesionalBase, 50L);
			ServicioDTO servicioDTO = new ServicioDTO(); // Asumiendo estructura del DTO de catálogo

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(profesionalServicioRepository.findByProfesionalId(1L)).thenReturn(List.of(ps));
			when(catalogoCliente.obtener(50L)).thenReturn(servicioDTO);

			List<ServicioDTO> resultado = profesionalService.listarServicios(1L);

			assertThat(resultado).hasSize(1);
			assertThat(resultado.get(0)).isEqualTo(servicioDTO);
			verify(catalogoCliente).obtener(50L);
		}

		@Test
		@DisplayName("asociarServicios con lista no nula debe limpiar anteriores, validar y guardar nuevos")
		void asociarServicios_ConListaIds_DebeReemplazarAsociaciones() {
			List<Long> servicioIds = List.of(10L, 20L);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(profesionalServicioRepository.findByProfesionalId(1L)).thenReturn(Collections.emptyList());

			List<ServicioDTO> resultado = profesionalService.asociarServicios(1L, servicioIds);

			assertThat(resultado).isEmpty();
			verify(profesionalServicioRepository).deleteByProfesionalId(1L);
			verify(catalogoCliente).obtener(10L);
			verify(catalogoCliente).obtener(20L);
			verify(profesionalServicioRepository, org.mockito.Mockito.times(2)).save(any(ProfesionalServicio.class));
		}

		@Test
		@DisplayName("asociarServicios con lista nula debe limpiar y retornar lista vacía")
		void asociarServicios_ConListaNull_SoloLimpia() {
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(profesionalServicioRepository.findByProfesionalId(1L)).thenReturn(Collections.emptyList());

			List<ServicioDTO> resultado = profesionalService.asociarServicios(1L, null);

			assertThat(resultado).isEmpty();
			verify(profesionalServicioRepository).deleteByProfesionalId(1L);
			verify(profesionalServicioRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Pruebas de Registro y Consulta de Ausencias")
	class AusenciasTests {

		@Test
		@DisplayName("registrarAusencia exitoso cuando la fecha fin es posterior o igual a la fecha inicio")
		void registrarAusencia_FechasValidas_DebeGuardar() {
			LocalDate inicio = LocalDate.now();
			LocalDate fin = inicio.plusDays(5);
			Ausencia ausenciaInput = new Ausencia(100L, null, inicio, fin, "Vacaciones");

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(ausenciaRepository.save(any(Ausencia.class))).thenAnswer(i -> i.getArgument(0));

			Ausencia resultado = profesionalService.registrarAusencia(1L, ausenciaInput);

			assertThat(resultado.getId()).isNull();
			assertThat(resultado.getProfesional()).isEqualTo(profesionalBase);
			verify(ausenciaRepository).save(ausenciaInput);
		}

		@Test
		@DisplayName("registrarAusencia debe lanzar ReglaNegocioException si fechaFin es antes de fechaInicio")
		void registrarAusencia_FechaFinAnterior_DebeLanzarExcepcion() {
			LocalDate inicio = LocalDate.now();
			LocalDate fin = inicio.minusDays(1);
			Ausencia ausenciaInput = new Ausencia(null, null, inicio, fin, "Incapacidad invalida");

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));

			assertThatThrownBy(() -> profesionalService.registrarAusencia(1L, ausenciaInput))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("La fecha de término no puede ser anterior a la de inicio");

			verify(ausenciaRepository, never()).save(any());
		}

		@Test
		@DisplayName("listarAusencias debe retornar la lista ordenada por fecha de inicio")
		void listarAusencias_DebeRetornarLista() {
			Ausencia ausencia = new Ausencia(1L, profesionalBase, LocalDate.now(), LocalDate.now().plusDays(1), "Permiso");
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(ausenciaRepository.findByProfesionalIdOrderByFechaInicioAsc(1L)).thenReturn(List.of(ausencia));

			List<Ausencia> resultado = profesionalService.listarAusencias(1L);

			assertThat(resultado).hasSize(1);
			verify(ausenciaRepository).findByProfesionalIdOrderByFechaInicioAsc(1L);
		}
	}

	@Nested
	@DisplayName("Pruebas de Historial de Citas")
	class HistorialTests {

		@Test
		@DisplayName("historial debe consultar al cliente de agenda si el profesional existe")
		void historial_ProfesionalExiste_DebeRetornarReservas() {
			LocalDate desde = LocalDate.now().minusDays(30);
			LocalDate hasta = LocalDate.now();
			String token = "Bearer token_xyz";
			ReservaDTO reservaDTO = new ReservaDTO();

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(agendaCliente.historial(1L, desde, hasta, token)).thenReturn(List.of(reservaDTO));

			List<ReservaDTO> resultado = profesionalService.historial(1L, desde, hasta, token);

			assertThat(resultado).hasSize(1);
			verify(agendaCliente).historial(1L, desde, hasta, token);
		}
	}
}