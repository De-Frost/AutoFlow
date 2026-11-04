package com.autoflow.service;

import com.autoflow.domain.Espacio;
import com.autoflow.domain.Reserva;
import com.autoflow.domain.Vehiculo;
import com.autoflow.repository.EspacioRepository;
import com.autoflow.repository.RecepcionRepository;
import com.autoflow.repository.ReservaRepository;
import com.autoflow.repository.VehiculoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;
    private final RecepcionRepository recepcionRepository;

    //HU-R06: reservas filtradas por fecha, estado y placa
    @Transactional(readOnly = true)
    public List<Reserva> buscar(LocalDate fecha, String estado, String placa) {
        List<Reserva> reservas;
        if (estado == null || estado.isBlank()) {
            reservas = reservaRepository.findAll();
        } else {
            reservas = reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(estado);
        }
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            boolean coincideFecha = fecha == null || r.getFecha().equals(fecha);
            boolean coincidePlaca = placa == null || placa.isBlank()
                    || r.getVehiculo().getPlaca().toUpperCase().contains(placa.trim().toUpperCase());
            if (coincideFecha && coincidePlaca) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    //Listas para el formulario de nueva reserva
    @Transactional(readOnly = true)
    public List<Vehiculo> getVehiculos() {
        return vehiculoRepository.findAllByOrderByPlacaAsc();
    }

    @Transactional(readOnly = true)
    public List<Espacio> getEspaciosActivos() {
        return espacioRepository.findByActivoTrueOrderByCodigoAsc();
    }

    //HU-R03: registra la reserva si el horario es valido y el espacio esta libre
    //Cada excepcion lleva la llave del mensaje de error (messages.properties)
    @Transactional
    public void registrar(Reserva reserva) {
        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException("reserva.error02");
        }
        LocalDateTime inicio = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("reserva.error03");
        }
        Vehiculo vehiculo = vehiculoRepository.findById(reserva.getVehiculo().getIdVehiculo())
                .orElseThrow(() -> new IllegalArgumentException("reserva.error01"));
        Espacio espacio = espacioRepository.findById(reserva.getEspacio().getIdEspacio())
                .orElseThrow(() -> new IllegalArgumentException("reserva.error01"));
        //Regla del profesor: no se permite si el espacio ya esta ocupado en ese horario
        //El 0 indica que es una reserva nueva (no hay ninguna que excluir)
        if (reservaRepository.existeConflicto(espacio.getIdEspacio(), reserva.getFecha(),
                reserva.getHoraInicio(), reserva.getHoraFin(), 0)) {
            throw new IllegalArgumentException("reserva.error04");
        }
        reserva.setVehiculo(vehiculo);
        reserva.setCliente(vehiculo.getCliente()); //El cliente es el duenno del vehiculo
        reserva.setEspacio(espacio);
        reserva.setEstado("Confirmada");
        reservaRepository.save(reserva);
    }

    //HU-R05: la reserva no se elimina, cambia a Cancelada y guarda el motivo
    @Transactional
    public void cancelar(Integer idReserva, String motivoCancelacion) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("reserva.error05"));
        if (!"Confirmada".equals(reserva.getEstado())) {
            throw new IllegalArgumentException("reserva.error06");
        }
        //Si el vehiculo ya llego al taller (modulo de Recepcion), la reserva ya no se puede cancelar
        if (recepcionRepository.existsByReservaIdReserva(idReserva)) {
            throw new IllegalArgumentException("reserva.error08");
        }
        if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
            throw new IllegalArgumentException("reserva.error07");
        }
        reserva.setEstado("Cancelada");
        reserva.setMotivoCancelacion(motivoCancelacion.trim());
        reservaRepository.save(reserva);
    }
}