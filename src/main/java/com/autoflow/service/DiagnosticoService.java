package com.autoflow.service;

import com.autoflow.domain.Diagnostico;
import com.autoflow.domain.Recepcion;
import com.autoflow.repository.DiagnosticoRepository;
import com.autoflow.repository.RecepcionRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoService {
    private final RecepcionRepository recepciones;
    private final DiagnosticoRepository diagnosticos;

    public List<Recepcion> pendientes(String busqueda) {
        String filtro = normalizar(busqueda == null ? "" : busqueda.trim());
        List<Recepcion> resultado = new ArrayList<>();
        for (Recepcion r : recepciones.findSinDiagnostico()) {
            if (normalizar(r.getVehiculo().getPlaca()).contains(filtro)
                    || normalizar(r.getVehiculo().getCliente().getNombre()).contains(filtro)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public Recepcion recepcion(Integer id) {
        return recepciones.findById(id).orElseThrow(() ->
                new IllegalArgumentException("diagnostico.error01"));
    }

    public boolean registrado(Integer id) {
        return diagnosticos.existsByRecepcionIdRecepcion(id);
    }

    @Transactional
    public Diagnostico registrar(Integer idRecepcion, String fallas, String trabajos) {
        var recepcion = recepcion(idRecepcion);
        if (registrado(idRecepcion)) {
            throw new IllegalArgumentException("diagnostico.error02");
        }
        validar(fallas, "diagnostico.error03", "diagnostico.error05");
        validar(trabajos, "diagnostico.error04", "diagnostico.error06");
        var diagnostico = new Diagnostico();
        diagnostico.setRecepcion(recepcion);
        diagnostico.setFallasEncontradas(fallas.strip());
        diagnostico.setTrabajosNecesarios(trabajos.strip());
        return diagnosticos.saveAndFlush(diagnostico);
    }

    private static void validar(String texto, String llaveVacio, String llaveLargo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(llaveVacio);
        }
        if (texto.length() > 10000) {
            throw new IllegalArgumentException(llaveLargo);
        }
    }
}
