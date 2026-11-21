package com.autoflow.service;

import com.autoflow.domain.Cliente;
import com.autoflow.repository.ClienteRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return clienteRepository.findAllByOrderByNombreAsc();
        }
        return clienteRepository
                .findByIdentificacionContainingIgnoreCaseOrNombreContainingIgnoreCaseOrderByNombreAsc(
                        texto.trim(), texto.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> getCliente(Integer idCliente) {
        return clienteRepository.findById(idCliente);
    }

    @Transactional
    public void save(Cliente cliente) {
        if (cliente.getIdCliente() == null) {
            cliente.setIdentificacion(cliente.getIdentificacion().trim());
            cliente.setNombre(cliente.getNombre().trim());
            cliente.setTelefono(cliente.getTelefono().trim());
            cliente.setCorreo(limpiarCorreo(cliente.getCorreo()));
            clienteRepository.save(cliente);
        } else {
            //Al editar se actualiza el registro existente: la identificacion y la cuenta no cambian
            var actual = clienteRepository.findById(cliente.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
            actual.setNombre(cliente.getNombre().trim());
            actual.setTelefono(cliente.getTelefono().trim());
            actual.setCorreo(limpiarCorreo(cliente.getCorreo()));
        }
        clienteRepository.flush();
    }

    @Transactional
    public void delete(Integer idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("El cliente con ID " + idCliente + " no existe.");
        }
        try {
            clienteRepository.deleteById(idCliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el cliente. Tiene datos asociados.", e);
        }
    }

    //El correo es opcional: si viene vacio se guarda como null
    private String limpiarCorreo(String correo) {
        return (correo == null || correo.isBlank()) ? null : correo.trim();
    }
}
