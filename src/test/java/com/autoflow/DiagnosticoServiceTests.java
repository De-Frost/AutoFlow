package com.autoflow;

import com.autoflow.domain.*;
import com.autoflow.repository.*;
import com.autoflow.service.DiagnosticoService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiagnosticoServiceTests {
    private final RecepcionRepository recepciones = mock(RecepcionRepository.class);
    private final DiagnosticoRepository diagnosticos = mock(DiagnosticoRepository.class);
    private final DiagnosticoService servicio = new DiagnosticoService(recepciones, diagnosticos);

    private Recepcion recepcion() {
        var cliente = new Cliente(); cliente.setNombre("Julián Alvarado");
        var vehiculo = new Vehiculo(); vehiculo.setPlaca("KAH387"); vehiculo.setCliente(cliente);
        var recepcion = new Recepcion(); recepcion.setVehiculo(vehiculo);
        when(recepciones.findById(1)).thenReturn(Optional.of(recepcion));
        return recepcion;
    }

    @Test void guardaDiagnosticoVinculadoALaRecepcion() {
        var recepcion = recepcion();
        when(diagnosticos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var resultado = servicio.registrar(1, "  Pastillas desgastadas  ", "Reemplazar pastillas\nRectificar discos");
        assertSame(recepcion, resultado.getRecepcion());
        assertEquals("Pastillas desgastadas", resultado.getFallasEncontradas());
        assertEquals("Reemplazar pastillas\nRectificar discos", resultado.getTrabajosNecesarios());
        assertNotNull(resultado.getFecha());
        assertNull(resultado.getMecanico());
    }

    @Test void impideDuplicados() {
        recepcion();
        when(diagnosticos.existsByRecepcionIdRecepcion(1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, "Falla", "Trabajo"));
        verify(diagnosticos, never()).saveAndFlush(any());
    }

    @Test void validaCamposObligatorios() {
        recepcion();
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, " \n ", "Trabajo"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, "Falla", null));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, "Falla", " "));
        verify(diagnosticos, never()).saveAndFlush(any());
    }

    @Test void rechazaTextoDemasiadoLargo() {
        recepcion();
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(1, "x".repeat(10001), "Trabajo"));
        verify(diagnosticos, never()).saveAndFlush(any());
    }

    @Test void rechazaRecepcionInexistente() {
        assertThrows(ResponseStatusException.class, () -> servicio.registrar(99, "Falla", "Trabajo"));
        verifyNoInteractions(diagnosticos);
    }

    @Test void buscaPorPlacaOClienteSinDistinguirMayusculasYAcentos() {
        var recepcion = recepcion();
        when(recepciones.findSinDiagnostico()).thenReturn(List.of(recepcion));
        assertEquals(List.of(recepcion), servicio.pendientes(" kah "));
        assertEquals(List.of(recepcion), servicio.pendientes("JULIAN"));
        assertEquals(List.of(recepcion), servicio.pendientes(null));
        assertTrue(servicio.pendientes("Otro cliente").isEmpty());
    }
}
