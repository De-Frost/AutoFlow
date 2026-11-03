package com.autoflow.service;

import com.autoflow.domain.*;
import com.autoflow.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnóstico no encontrado"));
    }

    public List<DetalleOrdenServicio> servicios(Integer id) {
        return detalles.findByOrdenIdOrden(id);
    }

    public record Linea(String descripcion, Integer cantidad, BigDecimal costo) { }

    @Transactional
    public OrdenServicio generar(Integer idDiagnostico, List<Linea> lineas) {
        var diagnostico = diagnostico(idDiagnostico);
        if (ordenes.existsByDiagnosticoIdDiagnostico(idDiagnostico)) {
            throw new IllegalArgumentException("Este diagnóstico ya tiene una orden de servicio.");
        }
        if (lineas == null || lineas.isEmpty() || lineas.size() > 50) {
            throw new IllegalArgumentException("Ingrese entre 1 y 50 servicios.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (var linea : lineas) {
            if (linea == null || linea.descripcion() == null || linea.descripcion().isBlank()
                    || linea.descripcion().length() > 150 || linea.cantidad() == null
                    || linea.cantidad() < 1 || linea.costo() == null
                    || linea.costo().signum() < 0 || linea.costo().scale() > 2) {
                throw new IllegalArgumentException("Revise la descripción, cantidad y costo de cada servicio (máximo dos decimales).");
            }
            total = total.add(linea.costo().multiply(BigDecimal.valueOf(linea.cantidad())));
        }
        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("El subtotal supera el monto permitido.");
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
            detalle.setDescripcion(linea.descripcion().trim());
            detalle.setCantidad(linea.cantidad());
            detalle.setCosto(linea.costo());
            detalles.save(detalle);
        }
        return orden;
    }
}
