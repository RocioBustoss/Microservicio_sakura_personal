package com.sakura.personal.model;

import java.time.LocalTime;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesional_id", nullable = false)
	private Profesional profesional;

	@NotNull(message = "El día es obligatorio")
	@Min(value = 1, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
	@Max(value = 7, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
	@Column(nullable = false)
	private Integer diaSemana;

	@NotNull(message = "La hora de inicio es obligatoria")
	@Column(nullable = false)
	private LocalTime horaInicio;

	@NotNull(message = "La hora de término es obligatoria")
	@Column(nullable = false)
	private LocalTime horaFin;

	private LocalTime horaAlmuerzoInicio;

	private LocalTime horaAlmuerzoFin;

}
