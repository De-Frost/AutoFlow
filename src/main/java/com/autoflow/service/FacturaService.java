package com.autoflow.service;

import com.autoflow.domain.DetalleOrdenRepuesto;
import com.autoflow.domain.DetalleOrdenServicio;
import com.autoflow.domain.Factura;
import com.autoflow.domain.OrdenServicio;
import com.autoflow.repository.DetalleOrdenRepuestoRepository;
import com.autoflow.repository.DetalleOrdenServicioRepository;
import com.autoflow.repository.FacturaRepository;
import com.autoflow.repository.OrdenServicioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacturaService {

    public static final String FINALIZADA = "Finalizada";

    private final FacturaRepository facturaRepository;
    private final OrdenServicioRepository ordenRepository;
    private final DetalleOrdenServicioRepository servicioRepository;
    private final DetalleOrdenRepuestoRepository repuestoRepository;

    //Ordenes Finalizadas
    @Transactional(readOnly = true)
    public List<OrdenServicio> getPendientes() {
        return ordenRepository.findPendientesFacturar();
    }

    //Facturas ya emitidas
    @Transactional(readOnly = true)
    public List<Factura> getFacturas() {
        return facturaRepository.findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Factura> getFactura(Integer idFactura) {
        return facturaRepository.findById(idFactura);
    }

    //Servicios
    @Transactional(readOnly = true)
    public List<DetalleOrdenServicio> getServicios(Integer idOrden) {
        return servicioRepository.findByOrdenIdOrden(idOrden);
    }

    //Repuestos consumidos
    @Transactional(readOnly = true)
    public List<DetalleOrdenRepuesto> getRepuestos(Integer idOrden) {
        return repuestoRepository.findByOrdenIdOrden(idOrden);
    }

    //Total de la orden
    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Integer idOrden) {
        BigDecimal total = BigDecimal.ZERO;
        for (var s : getServicios(idOrden)) {
            total = total.add(s.getCosto().multiply(BigDecimal.valueOf(s.getCantidad())));
        }
        for (var r : getRepuestos(idOrden)) {
            total = total.add(r.getPrecioUnitario().multiply(BigDecimal.valueOf(r.getCantidad())));
        }
        return total;
    }

    //Revisa que la orden se pueda facturar y la devuelve
    @Transactional(readOnly = true)
    public OrdenServicio getOrdenFacturable(Integer idOrden) {
        var orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("factura.error01"));
        if (!FINALIZADA.equals(orden.getEstado())) {
            throw new IllegalArgumentException("factura.error02");
        }
        if (facturaRepository.existsByOrdenIdOrden(idOrden)) {
            throw new IllegalArgumentException("factura.error03"); //Ya tiene factura
        }
        return orden;
    }

    //Genera la factura de una orden Finalizada
    @Transactional
    public Factura generar(Integer idOrden) {
        var orden = getOrdenFacturable(idOrden);
        var factura = new Factura();
        factura.setOrden(orden);
        factura.setFecha(LocalDateTime.now());
        factura.setTotal(calcularTotal(idOrden));
        return facturaRepository.save(factura);
    }
}