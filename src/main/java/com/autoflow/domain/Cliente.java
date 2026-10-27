package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "La identificacion es obligatoria")
    @Size(max = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 20)
    private String telefono;

    @Column(length = 75)
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 75)
    private String correo;

    //Solo existe si el cliente tiene cuenta
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;
}