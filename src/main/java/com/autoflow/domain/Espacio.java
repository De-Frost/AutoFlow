package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "espacio")
public class Espacio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_espacio")
    private Integer idEspacio;

    @Column(unique = true, nullable = false, length = 10)
    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 10)
    private String codigo;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    //Bahia o Elevador
    @Column(nullable = false, length = 20)
    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 20)
    private String tipo;

    //Un espacio inactivo no se puede usar para nuevas reservas
    @Column(nullable = false)
    private boolean activo = true;
}