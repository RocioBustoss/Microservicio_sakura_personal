package com.sakura.personal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesionales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesional {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 80, message = "El nombre no puede superar 80 caracteres")
	@Column(nullable = false, length = 80)
	private String nombre;

	@NotBlank(message = "El correo es obligatorio")
	@Email(message = "El correo no es válido")
	@Column(nullable = false, length = 250)
	private String correo;

	@NotBlank(message = "El teléfono es obligatorio")
	@Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
	@Column(nullable = false, length = 20)
	private String telefono;

	@NotBlank(message = "La especialidad es obligatoria")
	@Size(max = 80, message = "La especialidad no puede superar 80 caracteres")
	@Column(nullable = false, length = 80)
	private String especialidad;

	@Column(nullable = false)
	private boolean activo = true;

}
