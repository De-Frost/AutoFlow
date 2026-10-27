package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "diagnostico")
public class Diagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnostico")
    private Integer idDiagnostico;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Las fallas encontradas son obligatorias.")
    private String fallasEncontradas;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Los trabajos necesarios son obligatorios.")
    private String trabajosNecesarios;

    @OneToOne
    @JoinColumn(name = "id_recepcion", nullable = false, unique = true)
    private Recepcion recepcion;

    // Mecánico que registró el diagnóstico
    @ManyToOne
    @JoinColumn(name = "id_mecanico")
    private Usuario mecanico;
}