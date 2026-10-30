package com.autoflow.service;

import com.autoflow.domain.Cliente;
import com.autoflow.domain.Recepcion;
import com.autoflow.domain.Reserva;
import com.autoflow.domain.Vehiculo;
import com.autoflow.repository.ClienteRepository;
import com.autoflow.repository.RecepcionRepository;
import com.autoflow.repository.ReservaRepository;
import com.autoflow.repository.VehiculoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecepcionService {

    private final RecepcionRepository recepcionRepository;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;

    @Transactional(readOnly = true)
    public List<Recepcion> getRecepciones() {
        return recepcionRepository.findAllByOrderByFechaIngresoDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Recepcion> getRecepcion(Integer idRecepcion) {
        return recepcionRepository.findById(idRecepcion);
    }

    @Transactional(readOnly = true)
    public Optional<Reserva> getReserva(Integer idReserva) {
        return reservaRepository.findById(idReserva);
    }

    @Transactional(readOnly = true)
    public Optional<Vehiculo> getVehiculo(Integer idVehiculo) {
        return vehiculoRepository.findById(idVehiculo);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> getCliente(Integer idCliente) {
        return clienteRepository.findById(idCliente);
    }

    //Una reserva se puede recibir si esta Confirmada y aun no tiene recepcion
    @Transactional(readOnly = true)
    public boolean puedeRecibirse(Reserva reserva) {
        return "Confirmada".equals(reserva.getEstado())
                && !recepcionRepository.existsByReservaIdReserva(reserva.getIdReserva());
    }

    /* ---------- HU-R08: Reservas pendientes de recibir ---------- */
    //Se busca por numero de reserva, placa o nombre del cliente
    @Transactional(readOnly = true)
    public List<Reserva> getReservasPorRecibir(String buscar) {
        var reservas = reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc("Confirmada");
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (recepcionRepository.existsByReservaIdReserva(r.getIdReserva())) {
                continue; //Ya fue recibida
            }
            if (buscar == null || buscar.isBlank()) {
                resultado.add(r);
            } else {
                String texto = buscar.trim().toUpperCase();
                if (texto.equals(String.valueOf(r.getIdReserva()))
                        || r.getVehiculo().getPlaca().toUpperCase().contains(texto)
                        || r.getCliente().getNombre().toUpperCase().contains(texto)) {
                    resultado.add(r);
                }
            }
        }
        return resultado;
    }

    /* ---------- HU-R09: Buscar cliente y sus vehiculos ---------- */
    @Transactional(readOnly = true)
    public List<Cliente> buscarClientes(String buscar) {
        if (buscar == null || buscar.isBlank()) {
            return clienteRepository.findAllByOrderByNombreAsc();
        }
        return clienteRepository
                .findByIdentificacionContainingIgnoreCaseOrNombreContainingIgnoreCaseOrderByNombreAsc(
                        buscar.trim(), buscar.trim());
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> getVehiculosCliente(Integer idCliente) {
        return vehiculoRepository.findByClienteIdClienteOrderByPlacaAsc(idCliente);
    }

    /* ---------- HU-R08, HU-R09 y HU-R10: Guardar la recepcion con su estado inicial ---------- */
    //Las excepciones llevan la llave del mensaje (messages.properties)
    @Transactional
    public Recepcion guardar(Recepcion recepcion, Integer idReserva, Integer idVehiculo,
            boolean conDanos, boolean conObjetos) {
        //Si se marca Si, la descripcion es obligatoria
        if (conDanos) {
            if (recepcion.getDanosVisibles() == null || recepcion.getDanosVisibles().isBlank()) {
                throw new IllegalArgumentException("recepcion.error03");
            }
        } else {
            recepcion.setDanosVisibles(null);
        }
        if (conObjetos) {
            if (recepcion.getObjetosPersonales() == null || recepcion.getObjetosPersonales().isBlank()) {
                throw new IllegalArgumentException("recepcion.error04");
            }
        } else {
            recepcion.setObjetosPersonales(null);
        }

        if (idReserva != null) { //Llegada con reserva
            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new IllegalArgumentException("reserva.error01"));
            if (!"Confirmada".equals(reserva.getEstado())) {
                throw new IllegalArgumentException("recepcion.error05");
            }
            if (recepcionRepository.existsByReservaIdReserva(idReserva)) {
                throw new IllegalArgumentException("recepcion.error06");
            }
            recepcion.setReserva(reserva);
            recepcion.setVehiculo(reserva.getVehiculo());
        } else { //Llegada sin reserva
            if (idVehiculo == null) {
                throw new IllegalArgumentException("recepcion.error02");
            }
            Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                    .orElseThrow(() -> new IllegalArgumentException("recepcion.error02"));
            recepcion.setReserva(null);
            recepcion.setVehiculo(vehiculo);
        }
        recepcion.setIdRecepcion(null);
        recepcion.setFechaIngreso(LocalDateTime.now()); //Fecha y hora de llegada
        return recepcionRepository.save(recepcion);
    }
}
