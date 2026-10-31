package com.autoflow;

import com.autoflow.domain.*;
import com.autoflow.repository.*;
import com.autoflow.service.GenerarOrdenService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerarOrdenServiceTests {
    private final DiagnosticoRepository diagnosticos = mock(DiagnosticoRepository.class);
    private final OrdenServicioRepository ordenes = mock(OrdenServicioRepository.class);
    private final DetalleOrdenServicioRepository detalles = mock(DetalleOrdenServicioRepository.class);
    private final GenerarOrdenService servicio = new GenerarOrdenService(diagnosticos, ordenes, detalles);

    private void preparar() {
        when(diagnosticos.findById(1)).thenReturn(Optional.of(new Diagnostico()));
        when(ordenes.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }
    @Test void generaConCantidadesYSubtotalCalculado() {
        preparar();
        var orden = servicio.generar(1, List.of(
            new GenerarOrdenService.Linea("Pastillas", 2, new BigDecimal("35000.00")),
            new GenerarOrdenService.Linea("Discos", 1, new BigDecimal("25000.00"))));
        assertEquals(new BigDecimal("95000.00"), orden.getTotal());
        assertEquals("Pendiente", orden.getEstado());
        assertNull(orden.getMecanico());
        assertNotNull(orden.getFechaEstado());
        verify(detalles, times(2)).save(any());
    }
    @Test void rechazaDiagnosticoConOrden() {
        preparar();
        when(ordenes.existsByDiagnosticoIdDiagnostico(1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> servicio.generar(1,
            List.of(new GenerarOrdenService.Linea("Trabajo", 1, BigDecimal.TEN))));
        verify(ordenes, never()).saveAndFlush(any());
        verifyNoInteractions(detalles);
    }
    @Test void validaTodasLasLineasAntesDeGuardar() {
        preparar();
        assertThrows(IllegalArgumentException.class, () -> servicio.generar(1, List.of(
            new GenerarOrdenService.Linea("Trabajo", 1, BigDecimal.TEN),
            new GenerarOrdenService.Linea("Otro", 0, BigDecimal.TEN))));
        verify(ordenes, never()).saveAndFlush(any());
    }
    @Test void rechazaCostosNegativosYOrdenVacia() {
        preparar();
        assertThrows(IllegalArgumentException.class, () -> servicio.generar(1, List.of()));
        assertThrows(IllegalArgumentException.class, () -> servicio.generar(1,
            List.of(new GenerarOrdenService.Linea("Trabajo", 1, new BigDecimal("-1")))));
        verify(ordenes, never()).saveAndFlush(any());
    }
}
