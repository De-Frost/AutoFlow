package com.autoflow.service;

import com.autoflow.domain.Reserva;
import com.autoflow.domain.Recepcion;
import com.autoflow.repository.ReservaRepository;
import com.autoflow.repository.RecepcionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autoflow.domain.Vehiculo;
import com.autoflow.repository.VehiculoRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecepcionService {

    private final ReservaRepository reservaRepository;
    private final RecepcionRepository recepcionRepository;
    private final VehiculoRepository vehiculoRepository;

    // Buscar reservas pendientes de recepción
    public List<Reserva> buscarReservas(String busqueda) {
        return reservaRepository.buscarReservasPendientes(
                busqueda == null ? "" : busqueda.trim()
        );
    }

    // Obtener una reserva existente
    public Reserva obtenerReserva(Integer idReserva) {
        return reservaRepository.findById(idReserva)
                .orElseThrow(()
                        -> new IllegalArgumentException("recepcion.error01")
                );
    }

    // Comprobar si la reserva puede recibirse
    public Reserva validarLlegada(Integer idReserva) {

        Reserva reserva = obtenerReserva(idReserva);

        if (!"Confirmada".equals(reserva.getEstado())) {
            throw new IllegalArgumentException(
                    "recepcion.error02"
            );
        }

        if (recepcionRepository.existsByReservaIdReserva(idReserva)) {
            throw new IllegalArgumentException(
                    "recepcion.error03"
            );
        }

        return reserva;
    }

    // Registrar recepción con su estado inicial
    @Transactional
    public Recepcion registrarConReserva(
            Integer idReserva,
            Recepcion recepcion) {

        Reserva reserva = validarLlegada(idReserva);

        recepcion.setIdRecepcion(null);
        recepcion.setReserva(reserva);
        recepcion.setVehiculo(reserva.getVehiculo());

        return recepcionRepository.save(recepcion);
    }

    public List<Vehiculo> buscarVehiculos(String busqueda) {

        String texto = busqueda == null ? "" : busqueda.trim();

        return vehiculoRepository
                .findByPlacaContainingIgnoreCaseOrClienteNombreContainingIgnoreCaseOrderByPlacaAsc(
                        texto, texto);
    }

    public Vehiculo obtenerVehiculo(Integer idVehiculo) {

        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("recepcion.error04"));
    }

    @Transactional
    public Recepcion registrarSinReserva(Integer idVehiculo, Recepcion recepcion) {

        Vehiculo vehiculo = obtenerVehiculo(idVehiculo);

        recepcion.setIdRecepcion(null);
        recepcion.setVehiculo(vehiculo);
        recepcion.setReserva(null);

        return recepcionRepository.save(recepcion);
    }
}
