package com.autoflow.repository;

import com.autoflow.domain.OrdenServicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Integer> {

    public List<OrdenServicio> findAllByOrderByFechaDesc();

    public boolean existsByDiagnosticoIdDiagnostico(Integer idDiagnostico);

    public List<OrdenServicio> findByMecanicoIdUsuarioOrderByFechaDesc(Integer idMecanico);

    public long countByMecanicoIdUsuarioAndEstadoNot(Integer idMecanico, String estado);

    public List<OrdenServicio> findByEstadoOrderByFechaAsc(String estado);

    @Query("SELECT o FROM OrdenServicio o WHERE o.estado = 'Finalizada' AND NOT EXISTS "
            + "(SELECT f FROM Factura f WHERE f.orden = o) ORDER BY o.fecha")
    public List<OrdenServicio> findPendientesFacturar();
}