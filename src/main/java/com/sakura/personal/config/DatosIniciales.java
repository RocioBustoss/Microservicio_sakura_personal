package com.sakura.personal.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sakura.personal.model.Profesional;
import com.sakura.personal.model.ProfesionalServicio;
import com.sakura.personal.model.Turno;
import com.sakura.personal.repository.ProfesionalRepository;
import com.sakura.personal.repository.ProfesionalServicioRepository;
import com.sakura.personal.repository.TurnoRepository;

@Component
public class DatosIniciales implements CommandLineRunner {

	@Autowired
	private ProfesionalRepository profesionalRepository;

	@Autowired
	private TurnoRepository turnoRepository;

	@Autowired
	private ProfesionalServicioRepository profesionalServicioRepository;

	@Override
	public void run(String... args) {
		if (profesionalRepository.count() > 0) {
			return;
		}
		guardar("Carlos Pérez", "carlos@sakura.cl", "+56911111111", "Peluquería", List.of(1L, 2L, 3L, 4L));
		guardar("María Gómez", "maria@sakura.cl", "+56922222222", "Peluquería", List.of(1L, 2L, 3L, 4L));
		guardar("Valentina Soto", "valentina@sakura.cl", "+56933333333", "Manicure", List.of(5L, 6L, 7L, 8L));
		guardar("Alicia Varela", "alicia@sakura.cl", "+56944444444", "Cejas", List.of(9L, 10L, 11L, 12L));
		guardar("Sebastián Vasquez", "sebastian@sakura.cl", "+56955555555", "Peluquería", List.of(1L, 2L, 3L, 4L));
		guardar("Lucia Garrido", "lucia@sakura.cl", "+56966666666", "Manicure", List.of(5L, 6L, 7L, 8L));
	}

	private void guardar(String nombre, String correo, String telefono, String especialidad, List<Long> servicios) {
		Profesional profesional = new Profesional();
		profesional.setNombre(nombre);
		profesional.setCorreo(correo);
		profesional.setTelefono(telefono);
		profesional.setEspecialidad(especialidad);
		profesional.setActivo(true);
		profesional = profesionalRepository.save(profesional);
		for (int dia = 1; dia <= 5; dia++) {
			Turno turno = new Turno();
			turno.setProfesional(profesional);
			turno.setDiaSemana(dia);
			turno.setHoraInicio(LocalTime.of(9, 0));
			turno.setHoraFin(LocalTime.of(18, 0));
			turnoRepository.save(turno);
		}
		for (Long servicioId : servicios) {
			ProfesionalServicio vinculo = new ProfesionalServicio();
			vinculo.setProfesional(profesional);
			vinculo.setServicioId(servicioId);
			profesionalServicioRepository.save(vinculo);
		}
	}

}
