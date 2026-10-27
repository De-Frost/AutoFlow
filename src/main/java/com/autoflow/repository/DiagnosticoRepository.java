package com.autoflow.repository;

import com.autoflow.domain.Diagnostico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {

    public boolean existsByRecepcionIdRecepcion(Integer idRecepcion);

    @Query("SELECT d FROM Diagnostico d WHERE NOT EXISTS "
            + "(SELECT o FROM OrdenServicio o WHERE o.diagnostico = d) ORDER BY d.fecha")
    public List<Diagnostico> findSinOrden();
}