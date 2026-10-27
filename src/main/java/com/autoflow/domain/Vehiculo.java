package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehiculo")
public class Vehiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    //La placa identifica el vehiculo dentro del sistema
    @Column(unique = true, nullable = false, length = 10)
    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50)
    private String modelo;

    @NotNull(message = "El anio es obligatorio")
    @Min(value = 1900, message = "El anio no es valido")
    private Integer anio;

    //Cada vehiculo pertenece a un cliente
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}