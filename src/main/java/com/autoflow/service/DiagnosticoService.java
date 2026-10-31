package com.autoflow.service;

import com.autoflow.domain.Diagnostico;
import com.autoflow.domain.Recepcion;
import com.autoflow.repository.DiagnosticoRepository;
import com.autoflow.repository.RecepcionRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoService {
    private final RecepcionRepository recepciones;
    private final DiagnosticoRepository diagnosticos;

    public List<Recepcion> pendientes(String busqueda) {
        String filtro = normalizar(busqueda == null ? "" : busqueda.trim());
        return recepciones.findSinDiagnostico().stream().filter(r ->
                normalizar(r.getVehiculo().getPlaca()).contains(filtro)
                || normalizar(r.getVehiculo().getCliente().getNombre()).contains(filtro)).toList();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public Recepcion recepcion(Integer id) {
        return recepciones.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Recepción no encontrada"));
    }

    public boolean registrado(Integer id) {
        return diagnosticos.existsByRecepcionIdRecepcion(id);
    }

    @Transactional
    public Diagnostico registrar(Integer idRecepcion, String fallas, String trabajos) {
        var recepcion = recepcion(idRecepcion);
        if (registrado(idRecepcion)) {
            throw new IllegalArgumentException("Esta recepción ya tiene un diagnóstico registrado.");
        }
        validar(fallas, "Las fallas encontradas");
        validar(trabajos, "Los trabajos necesarios");
        var diagnostico = new Diagnostico();
        diagnostico.setRecepcion(recepcion);
        diagnostico.setFallasEncontradas(fallas.strip());
        diagnostico.setTrabajosNecesarios(trabajos.strip());
        return diagnosticos.saveAndFlush(diagnostico);
    }

    private static void validar(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " son obligatorios.");
        }
        if (texto.length() > 10000) {
            throw new IllegalArgumentException(campo + " no pueden superar los 10000 caracteres.");
        }
    }
}
