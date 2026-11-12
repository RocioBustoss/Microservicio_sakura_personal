package com.sakura.personal.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicioDTO {

	private Long id;

	private String nombre;

	private String descripcion;

	private Integer precio;

	private Integer duracionMinutos;

	private boolean activo;

	private CategoriaDTO categoria;

}
