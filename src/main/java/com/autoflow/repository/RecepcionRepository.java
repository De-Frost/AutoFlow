package com.autoflow.repository;

import com.autoflow.domain.Recepcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecepcionRepository extends JpaRepository<Recepcion, Integer> {

    public List<Recepcion> findAllByOrderByFechaIngresoDesc();

    public boolean existsByReservaIdReserva(Integer idReserva);

    @Query("SELECT r FROM Recepcion r WHERE NOT EXISTS "
            + "(SELECT d FROM Diagnostico d WHERE d.recepcion = r) ORDER BY r.fechaIngreso")
    public List<Recepcion> findSinDiagnostico();
}