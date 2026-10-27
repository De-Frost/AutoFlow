package com.autoflow.repository;

import com.autoflow.domain.Factura;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    public List<Factura> findAllByOrderByFechaDesc();

    public boolean existsByOrdenIdOrden(Integer idOrden);
}