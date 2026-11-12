package com.sakura.personal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sakura.personal.exception.RecursoNoEncontradoException;
import com.sakura.personal.exception.ReglaNegocioException;
import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.Turno;
import com.sakura.personal.repository.ProfesionalRepository;
import com.sakura.personal.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

	@Mock
	private TurnoRepository turnoRepository;

	@Mock
	private ProfesionalRepository profesionalRepository;

	@InjectMocks
	private TurnoService turnoService;

	private Profesional profesionalBase;

	@BeforeEach
	void setUp() {
		profesionalBase = new Profesional(1L, "Sakura Haruno", "sakura@konoha.com", "+56912345678", "Medicina General", true);
	}

	@Nested
	@DisplayName("Pruebas de Listado de Turnos")
	class ListarTurnosTests {

		@Test
		@DisplayName("listar debe retornar turnos ordenados por día y hora de inicio")
		void listar_DebeRetornarTurnosDelProfesional() {
			Turno turno1 = new Turno(10L, profesionalBase, 1, LocalTime.of(8, 0), LocalTime.of(12, 0), null, null);
			Turno turno2 = new Turno(11L, profesionalBase, 1, LocalTime.of(14, 0), LocalTime.of(18, 0), null, null);

			when(turnoRepository.findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(1L))
					.thenReturn(List.of(turno1, turno2));

			List<Turno> resultado = turnoService.listar(1L);

			assertThat(resultado).hasSize(2);
			assertThat(resultado.get(0).getId()).isEqualTo(10L);
			assertThat(resultado.get(1).getId()).isEqualTo(11L);
			verify(turnoRepository).findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(1L);
		}
	}

	@Nested
	@DisplayName("Pruebas de Guardado - Validaciones de Profesional y Horarios")
	class GuardarValidacionesBasicasTests {

		@Test
		@DisplayName("guardar debe lanzar RecursoNoEncontradoException cuando el profesional no existe")
		void guardar_ProfesionalInexistente_DebeLanzarExcepcion() {
			Turno turno = new Turno(null, null, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);
			when(profesionalRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> turnoService.guardar(99L, turno))
					.isInstanceOf(RecursoNoEncontradoException.class)
					.hasMessage("No existe el profesional");

			verify(turnoRepository, never()).save(any());
		}

		@Test
		@DisplayName("guardar debe lanzar ReglaNegocioException si horaInicio es igual a horaFin")
		void guardar_HoraInicioIgualAHoraFin_DebeLanzarExcepcion() {
			LocalTime hora = LocalTime.of(10, 0);
			Turno turno = new Turno(null, null, 1, hora, hora, null, null);
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));

			assertThatThrownBy(() -> turnoService.guardar(1L, turno))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("La hora de inicio debe ser anterior a la de término");

			verify(turnoRepository, never()).save(any());
		}

		@Test
		@DisplayName("guardar debe lanzar ReglaNegocioException si horaInicio es posterior a horaFin")
		void guardar_HoraInicioPosteriorAHoraFin_DebeLanzarExcepcion() {
			Turno turno = new Turno(null, null, 1, LocalTime.of(18, 0), LocalTime.of(9, 0), null, null);
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));

			assertThatThrownBy(() -> turnoService.guardar(1L, turno))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("La hora de inicio debe ser anterior a la de término");

			verify(turnoRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Pruebas de Guardado - Validaciones de Horario de Almuerzo")
	class ValidarAlmuerzoTests {

		@Test
		@DisplayName("guardar exitoso cuando el almuerzo es válido y queda dentro del turno")
		void guardar_AlmuerzoValido_DebeGuardarExitosamente() {
			Turno turno = new Turno(50L, null, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(13, 0), LocalTime.of(14, 0));

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(Collections.emptyList());
			when(turnoRepository.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

			Turno resultado = turnoService.guardar(1L, turno);

			assertThat(resultado.getId()).isNull();
			assertThat(resultado.getProfesional()).isEqualTo(profesionalBase);
			verify(turnoRepository).save(turno);
		}

		@Test
		@DisplayName("guardar exitoso cuando no se define ningún horario de almuerzo")
		void guardar_SinAlmuerzo_DebeGuardarExitosamente() {
			Turno turno = new Turno(null, null, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), null, null);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(Collections.emptyList());
			when(turnoRepository.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

			Turno resultado = turnoService.guardar(1L, turno);

			assertThat(resultado.getProfesional()).isEqualTo(profesionalBase);
			verify(turnoRepository).save(turno);
		}

		@ParameterizedTest
		@CsvSource({
			"13:00, '',      Almuerzo fin es nulo",
			"'',    14:00,   Almuerzo inicio es nulo",
			"14:00, 13:00,   Almuerzo fin antes de inicio",
			"13:00, 13:00,   Almuerzo inicio igual a fin",
			"08:00, 14:00,   Almuerzo inicio antes del turno (9:00)",
			"13:00, 19:00,   Almuerzo fin después del turno (18:00)"
		})
		@DisplayName("guardar debe rechazar configuraciones inválidas de horario de almuerzo")
		void guardar_AlmuerzoInvalido_DebeLanzarExcepcion(String inicioAlmuerzoStr, String finAlmuerzoStr, String causa) {
			LocalTime almuerzoInicio = inicioAlmuerzoStr != null && !inicioAlmuerzoStr.isBlank() ? LocalTime.parse(inicioAlmuerzoStr) : null;
			LocalTime almuerzoFin = finAlmuerzoStr != null && !finAlmuerzoStr.isBlank() ? LocalTime.parse(finAlmuerzoStr) : null;

			Turno turno = new Turno(null, null, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), almuerzoInicio, almuerzoFin);
			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));

			assertThatThrownBy(() -> turnoService.guardar(1L, turno))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("El almuerzo debe quedar dentro del turno");

			verify(turnoRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Pruebas de Guardado - Detección de Solapamientos")
	class SolapamientoTests {

		@Test
		@DisplayName("guardar exitoso cuando existen otros turnos el mismo día pero en horarios disjuntos")
		void guardar_TurnosSinSolapamiento_DebeGuardarExitosamente() {
			Turno nuevoTurno = new Turno(null, null, 1, LocalTime.of(14, 0), LocalTime.of(18, 0), null, null);
			Turno turnoExistente = new Turno(10L, profesionalBase, 1, LocalTime.of(8, 0), LocalTime.of(12, 0), null, null);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(List.of(turnoExistente));
			when(turnoRepository.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

			Turno resultado = turnoService.guardar(1L, nuevoTurno);

			assertThat(resultado).isNotNull();
			verify(turnoRepository).save(nuevoTurno);
		}

		@Test
		@DisplayName("guardar exitoso cuando el turno nuevo termina antes de que empiece el existente")
		void guardar_TurnoAnterior_NoSeSolapa() {
			Turno nuevoTurno = new Turno(null, null, 1, LocalTime.of(8, 0), LocalTime.of(10, 0), null, null);
			Turno turnoExistente = new Turno(10L, profesionalBase, 1, LocalTime.of(14, 0), LocalTime.of(18, 0), null, null);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(List.of(turnoExistente));
			when(turnoRepository.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

			Turno resultado = turnoService.guardar(1L, nuevoTurno);

			assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
			verify(turnoRepository).save(nuevoTurno);
		}

		@ParameterizedTest
		@CsvSource({
			"08:00, 12:00, Totalmente contenido dentro del turno existente (8:00-18:00)",
			"07:00, 10:00, Solape por izquierda (comienza antes y finaliza dentro)",
			"15:00, 19:00, Solape por derecha (comienza dentro y finaliza después)",
			"07:00, 19:00, Engloba completamente al turno existente"
		})
		@DisplayName("guardar debe lanzar ReglaNegocioException al solaparse con un turno existente")
		void guardar_TurnosSolapados_DebeLanzarExcepcion(String inicioStr, String finStr, String caso) {
			Turno nuevoTurno = new Turno(null, null, 1, LocalTime.parse(inicioStr), LocalTime.parse(finStr), null, null);
			Turno turnoExistente = new Turno(10L, profesionalBase, 1, LocalTime.of(8, 0), LocalTime.of(18, 0), null, null);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(List.of(turnoExistente));

			assertThatThrownBy(() -> turnoService.guardar(1L, nuevoTurno))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessage("El turno se solapa con otro horario del profesional");

			verify(turnoRepository, never()).save(any());
		}

		@Test
		@DisplayName("guardar exitoso cuando el turno es contiguo a otro (ej: uno termina justo cuando empieza el otro)")
		void guardar_TurnoContiguo_NoSeSolapa() {
			Turno nuevoTurno = new Turno(null, null, 1, LocalTime.of(12, 0), LocalTime.of(18, 0), null, null);
			Turno turnoExistente = new Turno(10L, profesionalBase, 1, LocalTime.of(8, 0), LocalTime.of(12, 0), null, null);

			when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesionalBase));
			when(turnoRepository.findByProfesionalIdAndDiaSemana(1L, 1)).thenReturn(List.of(turnoExistente));
			when(turnoRepository.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

			Turno resultado = turnoService.guardar(1L, nuevoTurno);

			assertThat(resultado).isNotNull();
			verify(turnoRepository).save(nuevoTurno);
		}
	}
}