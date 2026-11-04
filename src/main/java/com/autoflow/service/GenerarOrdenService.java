package com.autoflow.service;

import com.autoflow.domain.*;
import com.autoflow.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerarOrdenService {
    private final DiagnosticoRepository diagnosticos;
    private final OrdenServicioRepository ordenes;
    private final DetalleOrdenServicioRepository detalles;

    public List<Diagnostico> pendientes() { return diagnosticos.findSinOrden(); }

    public Diagnostico diagnostico(Integer id) {
        return diagnosticos.findById(id).orElseThrow(() ->
                new IllegalArgumentException("generar.error01"));
    }

    public List<DetalleOrdenServicio> servicios(Integer id) {
        return detalles.findByOrdenIdOrden(id);
    }

    @Transactional
    public OrdenServicio generar(Integer idDiagnostico, List<DetalleOrdenServicio> lineas) {
        var diagnostico = diagnostico(idDiagnostico);
        if (ordenes.existsByDiagnosticoIdDiagnostico(idDiagnostico)) {
            throw new IllegalArgumentException("generar.error02");
        }
        if (lineas == null || lineas.isEmpty() || lineas.size() > 50) {
            throw new IllegalArgumentException("generar.error03");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (var linea : lineas) {
            if (linea == null || linea.getDescripcion() == null || linea.getDescripcion().isBlank()
                    || linea.getDescripcion().length() > 150 || linea.getCantidad() == null
                    || linea.getCantidad() < 1 || linea.getCosto() == null
                    || linea.getCosto().signum() < 0 || linea.getCosto().scale() > 2) {
                throw new IllegalArgumentException("generar.error04");
            }
            total = total.add(linea.getCosto().multiply(BigDecimal.valueOf(linea.getCantidad())));
        }
        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("generar.error05");
        }
        var orden = new OrdenServicio();
        orden.setDiagnostico(diagnostico);
        orden.setEstado("Pendiente");
        orden.setFechaEstado(LocalDateTime.now());
        orden.setTotal(total);
        orden = ordenes.saveAndFlush(orden);
        for (var linea : lineas) {
            var detalle = new DetalleOrdenServicio();
            detalle.setOrden(orden);
            detalle.setDescripcion(linea.getDescripcion().trim());
            detalle.setCantidad(linea.getCantidad());
            detalle.setCosto(linea.getCosto());
            detalles.save(detalle);
        }
        return orden;
    }
}
