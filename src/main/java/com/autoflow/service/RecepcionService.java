package com.autoflow.service;

import com.autoflow.domain.Reserva;
import com.autoflow.domain.Recepcion;
import com.autoflow.repository.ReservaRepository;
import com.autoflow.repository.RecepcionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecepcionService {

    private final ReservaRepository reservaRepository;
    private final RecepcionRepository recepcionRepository;

    // Buscar reservas pendientes de recepción
    public List<Reserva> buscarReservas(String busqueda) {
        return reservaRepository.buscarReservasPendientes(
                busqueda == null ? "" : busqueda.trim()
        );
    }

    // Obtener una reserva existente
    public Reserva obtenerReserva(Integer idReserva) {
        return reservaRepository.findById(idReserva)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La reserva no existe"
                    )
                );
    }

    // Comprobar si la reserva puede recibirse
    public Reserva validarLlegada(Integer idReserva) {

        Reserva reserva = obtenerReserva(idReserva);

        if (!"Confirmada".equals(reserva.getEstado())) {
            throw new IllegalArgumentException(
                "La reserva no está confirmada"
            );
        }

        if (recepcionRepository.existsByReservaIdReserva(idReserva)) {
            throw new IllegalArgumentException(
                "Esta reserva ya tiene una recepción registrada"
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
}