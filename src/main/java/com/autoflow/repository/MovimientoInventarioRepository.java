package com.autoflow.repository;

import com.autoflow.domain.MovimientoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    public List<MovimientoInventario> findAllByOrderByFechaDesc();
}