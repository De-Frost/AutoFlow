package com.autoflow.repository;

import com.autoflow.domain.Espacio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Integer> {

    public List<Espacio> findAllByOrderByCodigoAsc();

    public List<Espacio> findByActivoTrueOrderByCodigoAsc();
}