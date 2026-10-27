package com.autoflow.repository;

import com.autoflow.domain.DetalleOrdenServicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleOrdenServicioRepository extends JpaRepository<DetalleOrdenServicio, Integer> {

    public List<DetalleOrdenServicio> findByOrdenIdOrden(Integer idOrden);
}