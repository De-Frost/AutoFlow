package com.autoflow.repository;

import com.autoflow.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    public Optional<Usuario> findByUsername(String username);
    
    public List<Usuario> findByRolNombreAndActivoTrueOrderByNombreAsc(String rol);
}