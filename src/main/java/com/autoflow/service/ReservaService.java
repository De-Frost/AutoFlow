package com.autoflow.service;

import com.autoflow.domain.Cliente;
import com.autoflow.domain.Espacio;
import com.autoflow.domain.Reserva;
import com.autoflow.domain.Vehiculo;
import com.autoflow.repository.ClienteRepository;
import com.autoflow.repository.EspacioRepository;
import com.autoflow.repository.RecepcionRepository;
import com.autoflow.repository.ReservaRepository;
import com.autoflow.repository.VehiculoRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    //Horario de atencion del taller: franjas de una hora entre las 8:00 y las 17:00
    public static final int HORA_APERTURA = 8;
    public static final int HORA_CIERRE = 17;

    private final ReservaRepository reservaRepository;
    private final RecepcionRepository recepcionRepository;
    private final EspacioRepository espacioRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;

    /* ---------- Listas para los formularios ---------- */
    @Transactional(readOnly = true)
    public List<Espacio> getEspacios(boolean activos) {
        if (activos) { //Solo activos, para nuevas reservas
            return espacioRepository.findByActivoTrueOrderByCodigoAsc();
        }
        return espacioRepository.findAllByOrderByCodigoAsc();
    }

    @Transactional(readOnly = true)
    public List<Cliente> getClientes() {
        return clienteRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> getVehiculos() {
        return vehiculoRepository.findAllByOrderByPlacaAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Reserva> getReserva(Integer idReserva) {
        return reservaRepository.findById(idReserva);
    }

    /* ---------- HU-R06: Listado de reservas con filtros ---------- */
    @Transactional(readOnly = true)
    public List<Reserva> buscar(LocalDate fecha, String estado, String placa) {
        List<Reserva> reservas;
        if (estado != null && !estado.isBlank()) {
            reservas = reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(estado);
        } else {
            reservas = reservaRepository.findAll();
        }
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            boolean coincide = true;
            if (fecha != null && !r.getFecha().equals(fecha)) {
                coincide = false;
            }
            if (placa != null && !placa.isBlank()
                    && !r.getVehiculo().getPlaca().toUpperCase().contains(placa.trim().toUpperCase())) {
                coincide = false;
            }
            if (coincide) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /* ---------- HU-R02: Disponibilidad de espacios ---------- */
    //Franjas horarias de la cuadricula: "08:00", "09:00", ... "16:00"
    public List<String> getFranjas() {
        List<String> franjas = new ArrayList<>();
        for (int h = HORA_APERTURA; h < HORA_CIERRE; h++) {
            franjas.add(LocalTime.of(h, 0).toString());
        }
        return franjas;
    }

    //Franjas que ya pasaron (No disponible)
    public List<String> getFranjasPasadas(LocalDate fecha) {
        List<String> pasadas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        for (int h = HORA_APERTURA; h < HORA_CIERRE; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            if (fecha.isBefore(hoy) || (fecha.equals(hoy) && inicio.isBefore(ahora))) {
                pasadas.add(inicio.toString());
            }
        }
        return pasadas;
    }

    //Franjas ocupadas por reservas no canceladas. Llave: codigo del espacio + " " + franja (ej: "E-01 08:00")
    @Transactional(readOnly = true)
    public Map<String, Reserva> getOcupacion(LocalDate fecha) {
        Map<String, Reserva> ocupacion = new HashMap<>();
        var reservas = reservaRepository.findByFechaAndEstadoNotOrderByHoraInicioAsc(fecha, "Cancelada");
        for (Reserva r : reservas) {
            for (int h = HORA_APERTURA; h < HORA_CIERRE; h++) {
                LocalTime inicio = LocalTime.of(h, 0);
                LocalTime fin = inicio.plusHours(1);
                //La reserva ocupa la franja si se traslapa total o parcialmente con ella
                if (r.getHoraInicio().isBefore(fin) && r.getHoraFin().isAfter(inicio)) {
                    ocupacion.put(r.getEspacio().getCodigo() + " " + inicio, r);
                }
            }
        }
        return ocupacion;
    }

    /* ---------- Reglas de la reserva ---------- */
    //Una reserva se puede modificar o cancelar si esta Confirmada y el vehiculo aun no se ha recibido
    @Transactional(readOnly = true)
    public boolean esModificable(Reserva reserva) {
        return "Confirmada".equals(reserva.getEstado())
                && !recepcionRepository.existsByReservaIdReserva(reserva.getIdReserva());
    }

    @Transactional(readOnly = true)
    public boolean tieneRecepcion(Integer idReserva) {
        return recepcionRepository.existsByReservaIdReserva(idReserva);
    }

    //Valida el horario y el espacio. Las excepciones llevan la llave del mensaje (messages.properties)
    private void validarHorario(Reserva reserva, Integer idReserva) {
        if (reserva.getEspacio() == null || reserva.getEspacio().getIdEspacio() == null) {
            throw new IllegalArgumentException("reserva.error02");
        }
        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException("reserva.error03");
        }
        LocalDate hoy = LocalDate.now();
        if (reserva.getFecha().isBefore(hoy)
                || (reserva.getFecha().equals(hoy) && reserva.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new IllegalArgumentException("reserva.error04");
        }
        Espacio espacio = espacioRepository.findById(reserva.getEspacio().getIdEspacio())
                .orElseThrow(() -> new IllegalArgumentException("reserva.error02"));
        if (!espacio.isActivo()) {
            throw new IllegalArgumentException("reserva.error06");
        }
        if (reservaRepository.existeConflicto(espacio.getIdEspacio(), reserva.getFecha(),
                reserva.getHoraInicio(), reserva.getHoraFin(), idReserva)) {
            throw new IllegalArgumentException("reserva.error05");
        }
        reserva.setEspacio(espacio);
    }

    /* ---------- HU-R03: Registrar reserva ---------- */
    @Transactional
    public void registrar(Reserva reserva) {
        if (reserva.getCliente() == null || reserva.getCliente().getIdCliente() == null
                || reserva.getVehiculo() == null || reserva.getVehiculo().getIdVehiculo() == null) {
            throw new IllegalArgumentException("reserva.error02");
        }
        Vehiculo vehiculo = vehiculoRepository.findById(reserva.getVehiculo().getIdVehiculo())
                .orElseThrow(() -> new IllegalArgumentException("reserva.error02"));
        if (!vehiculo.getCliente().getIdCliente().equals(reserva.getCliente().getIdCliente())) {
            throw new IllegalArgumentException("reserva.error07");
        }
        validarHorario(reserva, 0); //Reserva nueva: no hay que excluir ninguna
        reserva.setIdReserva(null);
        reserva.setVehiculo(vehiculo);
        reserva.setCliente(vehiculo.getCliente());
        reserva.setEstado("Confirmada");
        reserva.setMotivoCancelacion(null);
        reservaRepository.save(reserva);
    }

    /* ---------- HU-R04: Modificar reserva ---------- */
    @Transactional
    public Reserva modificar(Reserva datos) {
        Reserva reserva = reservaRepository.findById(datos.getIdReserva())
                .orElseThrow(() -> new IllegalArgumentException("reserva.error01"));
        if (!esModificable(reserva)) {
            throw new IllegalArgumentException("reserva.error08");
        }
        //Se valida sin tomar en cuenta la propia reserva que se modifica
        validarHorario(datos, reserva.getIdReserva());
        reserva.setEspacio(datos.getEspacio());
        reserva.setFecha(datos.getFecha());
        reserva.setHoraInicio(datos.getHoraInicio());
        reserva.setHoraFin(datos.getHoraFin());
        reserva.setMotivo(datos.getMotivo());
        return reservaRepository.save(reserva);
    }

    /* ---------- HU-R05: Cancelar reserva (no se elimina) ---------- */
    @Transactional
    public Reserva cancelar(Integer idReserva, String motivoCancelacion) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("reserva.error01"));
        if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
            throw new IllegalArgumentException("reserva.error09");
        }
        if (!esModificable(reserva)) {
            throw new IllegalArgumentException("reserva.error08");
        }
        reserva.setEstado("Cancelada");
        reserva.setMotivoCancelacion(motivoCancelacion.trim());
        return reservaRepository.save(reserva);
    }
}
