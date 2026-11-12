package com.sakura.personal.model;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesional_servicio", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "profesional_id", "servicio_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalServicio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profesional_id", nullable = false)
	private Profesional profesional;

	@Column(name = "servicio_id", nullable = false)
	private Long servicioId;

}
