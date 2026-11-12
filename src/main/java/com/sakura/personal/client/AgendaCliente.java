package com.sakura.personal.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sakura.personal.exception.ReglaNegocioException;

@Component
public class AgendaCliente {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${sakura.agenda.url}")
	private String baseUrl;

	public boolean tieneReservas(Long profesionalId, String authorization) {
		try {
			Map<String, Boolean> cuerpo = restTemplate.exchange(
					baseUrl + "/api/v1/reservas/profesional/" + profesionalId + "/existe", HttpMethod.GET,
					conAuth(authorization), new ParameterizedTypeReference<Map<String, Boolean>>() {
					}).getBody();
			return cuerpo != null && Boolean.TRUE.equals(cuerpo.get("existe"));
		} catch (RestClientException ex) {
			throw new ReglaNegocioException("No se pudo consultar la agenda");
		}
	}

	public List<ReservaDTO> historial(Long profesionalId, LocalDate desde, LocalDate hasta, String authorization) {
		try {
			String url = baseUrl + "/api/v1/reservas/historial?profesionalId=" + profesionalId + "&desde=" + desde
					+ "&hasta=" + hasta;
			List<ReservaDTO> cuerpo = restTemplate.exchange(url, HttpMethod.GET, conAuth(authorization),
					new ParameterizedTypeReference<List<ReservaDTO>>() {
					}).getBody();
			return cuerpo == null ? List.of() : cuerpo;
		} catch (RestClientException ex) {
			throw new ReglaNegocioException("No se pudo consultar el historial");
		}
	}

	private HttpEntity<Void> conAuth(String authorization) {
		HttpHeaders headers = new HttpHeaders();
		if (authorization != null) {
			headers.set(HttpHeaders.AUTHORIZATION, authorization);
		}
		return new HttpEntity<>(headers);
	}

}
