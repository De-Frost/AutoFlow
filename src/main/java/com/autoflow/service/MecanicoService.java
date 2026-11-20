package com.autoflow.service;

import com.autoflow.repository.OrdenServicioRepository;
import com.autoflow.repository.UsuarioRepository;
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
public class MecanicoService {

    private final UsuarioRepository usuarios;
    private final OrdenServicioRepository ordenes;

    @Getter
    @AllArgsConstructor
    public static class Resumen {
        private Integer id;
        private String nombre;
        private boolean activo;
        private long enCurso;
        private long finalizadas;
    }

    //Todos los mecanicos con sus ordenes en curso y finalizadas
    public List<Resumen> listar() {
        List<Resumen> resultado = new ArrayList<>();
        for (var u : usuarios.findByRolNombreOrderByNombreAsc("MECANICO")) {
            long finalizadas = ordenes.countByMecanicoIdUsuarioAndEstado(u.getIdUsuario(), "Finalizada");
            long enCurso = ordenes.countByMecanicoIdUsuarioAndEstadoNot(u.getIdUsuario(), "Finalizada");
            resultado.add(new Resumen(u.getIdUsuario(), u.getNombre(), u.isActivo(), enCurso, finalizadas));
        }
        return resultado;
    }
}
