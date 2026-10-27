package com.autoflow.repository;

import com.autoflow.domain.Repuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    public List<Repuesto> findByActivoTrue();

    public List<Repuesto> findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(String codigo, String nombre);
}