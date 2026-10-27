package com.autoflow.repository;

import com.autoflow.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    public Optional<Rol> findByNombre(String nombre);
}