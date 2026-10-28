package com.autoflow.service;

import com.autoflow.domain.Repuesto;
import com.autoflow.repository.RepuestoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;

    @Transactional(readOnly = true)
    public List<Repuesto> getRepuestos(boolean activo) {
        if (activo) { //Sólo activos...
            return repuestoRepository.findByActivoTrue();
        }
        return repuestoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Repuesto> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return repuestoRepository.findAll();
        }
        return repuestoRepository
                .findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(texto.trim(), texto.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Repuesto> getRepuesto(Integer idRepuesto) {
        return repuestoRepository.findById(idRepuesto);
    }

    @Transactional
    public void save(Repuesto repuesto) {
        repuesto.setCodigo(repuesto.getCodigo().trim().toUpperCase());
        repuestoRepository.save(repuesto);
        repuestoRepository.flush();
    }

    @Transactional
    public void delete(Integer idRepuesto) {
        if (!repuestoRepository.existsById(idRepuesto)) {
            throw new IllegalArgumentException("El repuesto con ID " + idRepuesto + " no existe.");
        }
        try {
            repuestoRepository.deleteById(idRepuesto);
            repuestoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el repuesto. Tiene datos asociados.", e);
        }
    }
}