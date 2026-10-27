package com.autoflow.repository;

import com.autoflow.domain.Vehiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    public List<Vehiculo> findAllByOrderByPlacaAsc();

    public List<Vehiculo> findByClienteIdClienteOrderByPlacaAsc(Integer idCliente);

    public List<Vehiculo> findByPlacaContainingIgnoreCaseOrClienteNombreContainingIgnoreCaseOrderByPlacaAsc(
            String placa, String nombreCliente);
}