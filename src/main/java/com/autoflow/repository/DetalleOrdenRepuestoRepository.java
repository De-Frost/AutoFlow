package com.autoflow.repository;

import com.autoflow.domain.DetalleOrdenRepuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleOrdenRepuestoRepository extends JpaRepository<DetalleOrdenRepuesto, Integer> {

    public List<DetalleOrdenRepuesto> findByOrdenIdOrden(Integer idOrden);
}