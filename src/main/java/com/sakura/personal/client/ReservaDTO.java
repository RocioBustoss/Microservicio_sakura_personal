package com.sakura.personal.client;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaDTO {

	private Long id;

	private Long profesionalId;

	private String nombreServicio;

	private String nombreProfesional;

	private LocalDate fecha;

	private LocalTime horaInicio;

	private LocalTime horaFin;

	private String estado;

	private Integer montoPagado;

	private String nombreCliente;

}
