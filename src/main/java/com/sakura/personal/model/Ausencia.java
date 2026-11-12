package com.sakura.personal.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ausencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ausencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesional_id", nullable = false)
	private Profesional profesional;

	@NotNull(message = "La fecha de inicio es obligatoria")
	@Column(nullable = false)
	private LocalDate fechaInicio;

	@NotNull(message = "La fecha de término es obligatoria")
	@Column(nullable = false)
	private LocalDate fechaFin;

	@Size(max = 200, message = "El motivo no puede superar 200 caracteres")
	@Column(length = 200)
	private String motivo;

}
