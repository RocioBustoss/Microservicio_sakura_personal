package com.sakura.personal.exception;

public class NoAutenticadoException extends RuntimeException {
	public NoAutenticadoException(String mensaje) {
		super(mensaje);
	}
}
