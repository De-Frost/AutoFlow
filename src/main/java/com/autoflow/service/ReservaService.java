package com.autoflow.service;

import com.autoflow.domain.Reserva;
import com.autoflow.repository.ReservaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;

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
}