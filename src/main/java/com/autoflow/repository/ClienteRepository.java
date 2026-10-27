package com.autoflow.repository;

import com.autoflow.domain.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    public List<Cliente> findAllByOrderByNombreAsc();

    public List<Cliente> findByIdentificacionContainingIgnoreCaseOrNombreContainingIgnoreCaseOrderByNombreAsc(
            String identificacion, String nombre);
}