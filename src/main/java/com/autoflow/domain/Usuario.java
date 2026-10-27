package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(unique = true, nullable = false, length = 30)
    @NotBlank
    @Size(max = 30)
    private String username;

    @Column(nullable = false, length = 512)
    @NotBlank
    private String password;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;

    //Un rol tiene muchos usuarios
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}