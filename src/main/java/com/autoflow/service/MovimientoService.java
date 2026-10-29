package com.autoflow.service;

import com.autoflow.domain.DetalleOrdenRepuesto;
import com.autoflow.domain.MovimientoInventario;
import com.autoflow.domain.OrdenServicio;
import com.autoflow.domain.Repuesto;
import com.autoflow.repository.DetalleOrdenRepuestoRepository;
import com.autoflow.repository.MovimientoInventarioRepository;
import com.autoflow.repository.OrdenServicioRepository;
import com.autoflow.repository.RepuestoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    public static final String SALIDA = "SALIDA";
    public static final String FINALIZADA = "Finalizada";

    private final MovimientoInventarioRepository movimientoRepository;
    private final DetalleOrdenRepuestoRepository detalleRepository;
    private final RepuestoRepository repuestoRepository;
    private final OrdenServicioRepository ordenRepository;

    // Historial de entradas y salidas del inventario
    @Transactional(readOnly = true)
    public List<MovimientoInventario> getMovimientos() {
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    // Órdenes a las que todavía se les pueden cargar repuestos: las que no están Finalizadas
    @Transactional(readOnly = true)
    public List<OrdenServicio> getOrdenesAbiertas() {
        return ordenRepository.findAllByOrderByFechaDesc().stream()
                .filter(o -> !FINALIZADA.equals(o.getEstado()))
                .toList();
    }

    //Solo los repuestos activos se pueden usar
    @Transactional(readOnly = true)
    public List<Repuesto> getRepuestosActivos() {
        return repuestoRepository.findByActivoTrue();
    }

    //Repuestos que ya se usaron en una orden
    @Transactional(readOnly = true)
    public List<DetalleOrdenRepuesto> getRepuestosDeOrden(Integer idOrden) {
        return detalleRepository.findByOrdenIdOrden(idOrden);
    }

    //Valida la orden, el repuesto y la cantidad, descuenta la cantidad, guarda el repuesto, la cantidad y el precio en la orden y guardan el movimiento de SALIDA
    @Transactional
    public MovimientoInventario registrarConsumo(Integer idOrden, Integer idRepuesto, Integer cantidad) {
        var orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("movimiento.error01"));
        //A una orden finalizada ya no se le cargan repuestos
        if (FINALIZADA.equals(orden.getEstado())) {
            throw new IllegalArgumentException("movimiento.error02");
        }
        var repuesto = repuestoRepository.findById(idRepuesto)
                .filter(r -> r.isActivo())
                .orElseThrow(() -> new IllegalArgumentException("movimiento.error03"));
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("movimiento.error04");
        }
        //No se puede usar mas de lo que hay en inventario
        if (cantidad > repuesto.getCantidadDisponible()) {
            throw new IllegalArgumentException("movimiento.error05");
        }

        //Se descuenta del inventario
        int stockAnterior = repuesto.getCantidadDisponible();
        int stockNuevo = stockAnterior - cantidad;
        repuesto.setCantidadDisponible(stockNuevo);
        repuestoRepository.save(repuesto);

        //El repuesto queda en la orden con el precio de hoy
        var detalle = new DetalleOrdenRepuesto();
        detalle.setOrden(orden);
        detalle.setRepuesto(repuesto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(repuesto.getPrecio());
        detalleRepository.save(detalle);

        //Movimiento de inventario
        var movimiento = new MovimientoInventario();
        movimiento.setTipo(SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setRepuesto(repuesto);
        movimiento.setOrden(orden);
        return movimientoRepository.save(movimiento);
    }
}