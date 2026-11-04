package com.autoflow.service;

import com.autoflow.domain.OrdenServicio;
import com.autoflow.domain.Usuario;
import com.autoflow.repository.OrdenServicioRepository;
import com.autoflow.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenServicioService {
    private final OrdenServicioRepository ordenes;
    private final UsuarioRepository usuarios;
    public static final List<String> ESTADOS = List.of("En proceso", "Pendiente", "Finalizada");

    public List<OrdenServicio> listar() {
        return ordenes.findAllByOrderByFechaDesc();
    }

    public OrdenServicio obtener(Integer id) {
        return ordenes.findById(id).orElseThrow(() ->
                new IllegalArgumentException("orden.error01"));
    }

    @Getter
    @AllArgsConstructor
    public static class MecanicoDisponible {
        private Integer id;
        private String nombre;
        private long carga;
    }

    public List<MecanicoDisponible> mecanicos() {
        List<MecanicoDisponible> disponibles = new ArrayList<>();
        for (var u : usuarios.findByRolNombreAndActivoTrueOrderByNombreAsc("MECANICO")) {
            disponibles.add(new MecanicoDisponible(u.getIdUsuario(), u.getNombre(),
                    ordenes.countByMecanicoIdUsuarioAndEstadoNot(u.getIdUsuario(), "Finalizada")));
        }
        return disponibles;
    }

    public long carga(Usuario mecanico) {
        return ordenes.countByMecanicoIdUsuarioAndEstadoNot(mecanico.getIdUsuario(), "Finalizada");
    }

    @Transactional
    public void asignar(Integer idOrden, Integer idMecanico) {
        var orden = obtener(idOrden);
        if ("Finalizada".equals(orden.getEstado())) {
            throw new IllegalArgumentException("orden.error02");
        }
        var mecanico = usuarios.findById(idMecanico)
                .orElseThrow(() -> new IllegalArgumentException("orden.error03"));
        if (!mecanico.isActivo() || mecanico.getRol() == null
                || !"MECANICO".equals(mecanico.getRol().getNombre())) {
            throw new IllegalArgumentException("orden.error04");
        }
        orden.setMecanico(mecanico);
        ordenes.save(orden);
    }

    @Transactional
    public void actualizarEstado(Integer idOrden, String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("orden.error05");
        }
        var orden = obtener(idOrden);
        if (orden.getMecanico() == null && !"Pendiente".equals(estado)) {
            throw new IllegalArgumentException("orden.error06");
        }
        if (!estado.equals(orden.getEstado())) {
            orden.setEstado(estado);
            orden.setFechaEstado(LocalDateTime.now());
            ordenes.save(orden);
        }
    }
}
