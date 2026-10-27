package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rol")
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    //ADMINISTRADOR, RECEPCIONISTA, MECANICO o CLIENTE
    @Column(unique = true, nullable = false, length = 30)
    @NotBlank
    @Size(max = 30)
    private String nombre;
}