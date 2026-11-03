package com.autoflow;

import com.autoflow.domain.*;
import com.autoflow.repository.*;
import com.autoflow.service.OrdenServicioService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrdenServicioServiceTests {
    private final OrdenServicioRepository ordenes = mock(OrdenServicioRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final OrdenServicioService servicio = new OrdenServicioService(ordenes, usuarios);

    private OrdenServicio orden() {
        var orden = new OrdenServicio();
        when(ordenes.findById(1)).thenReturn(Optional.of(orden));
        return orden;
    }

    private Usuario mecanico(boolean activo) {
        var rol = new Rol();
        rol.setNombre("MECANICO");
        var usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setActivo(activo);
        when(usuarios.findById(2)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test void asignaMecanicoActivo() {
        var orden = orden();
        var usuario = mecanico(true);
        servicio.asignar(1, 2);
        assertSame(usuario, orden.getMecanico());
        verify(ordenes).save(orden);
    }

    @Test void rechazaMecanicoInactivo() {
        orden();
        mecanico(false);
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(1, 2));
        verify(ordenes, never()).save(any());
    }

    @Test void rechazaUsuarioDeOtroRol() {
        orden();
        mecanico(true).getRol().setNombre("ADMINISTRADOR");
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(1, 2));
    }

    @Test void rechazaAsignarOrdenFinalizada() {
        orden().setEstado("Finalizada");
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(1, 2));
        verifyNoInteractions(usuarios);
    }

    @Test void actualizaEstadoYFecha() {
        var orden = orden();
        orden.setMecanico(mecanico(true));
        servicio.actualizarEstado(1, "En proceso");
        assertEquals("En proceso", orden.getEstado());
        assertNotNull(orden.getFechaEstado());
        verify(ordenes).save(orden);
    }

    @Test void rechazaEstadoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarEstado(1, "Otro"));
        verifyNoInteractions(ordenes);
    }

    @Test void requiereMecanicoParaIniciarTrabajo() {
        orden();
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarEstado(1, "En proceso"));
        verify(ordenes, never()).save(any());
    }
}
