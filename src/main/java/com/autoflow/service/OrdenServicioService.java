package com.autoflow.service;

import com.autoflow.domain.OrdenServicio;
import com.autoflow.domain.Usuario;
import com.autoflow.repository.OrdenServicioRepository;
import com.autoflow.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
    }

    public record MecanicoDisponible(Integer id, String nombre, long carga) { }

    public List<MecanicoDisponible> mecanicos() {
        return usuarios.findByRolNombreAndActivoTrueOrderByNombreAsc("MECANICO").stream()
                .map(u -> new MecanicoDisponible(u.getIdUsuario(), u.getNombre(),
                        ordenes.countByMecanicoIdUsuarioAndEstadoNot(u.getIdUsuario(), "Finalizada")))
                .toList();
    }

    public long carga(Usuario mecanico) {
        return ordenes.countByMecanicoIdUsuarioAndEstadoNot(mecanico.getIdUsuario(), "Finalizada");
    }

    @Transactional
    public void asignar(Integer idOrden, Integer idMecanico) {
        var orden = obtener(idOrden);
        if ("Finalizada".equals(orden.getEstado())) {
            throw new IllegalArgumentException("No se puede asignar una orden finalizada.");
        }
        var mecanico = usuarios.findById(idMecanico)
                .orElseThrow(() -> new IllegalArgumentException("El mecánico no existe."));
        if (!mecanico.isActivo() || mecanico.getRol() == null
                || !"MECANICO".equals(mecanico.getRol().getNombre())) {
            throw new IllegalArgumentException("Seleccione un mecánico activo.");
        }
        orden.setMecanico(mecanico);
        ordenes.save(orden);
    }

    @Transactional
    public void actualizarEstado(Integer idOrden, String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("El estado seleccionado no es válido.");
        }
        var orden = obtener(idOrden);
        if (orden.getMecanico() == null && !"Pendiente".equals(estado)) {
            throw new IllegalArgumentException("Asigne un mecánico antes de iniciar o finalizar el trabajo.");
        }
        if (!estado.equals(orden.getEstado())) {
            orden.setEstado(estado);
            orden.setFechaEstado(LocalDateTime.now());
            ordenes.save(orden);
        }
    }
}
