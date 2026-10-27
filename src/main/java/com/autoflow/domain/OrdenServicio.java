package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orden_servicio")
public class OrdenServicio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer idOrden;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    //Pendiente, En proceso o Finalizada
    @Column(nullable = false, length = 20)
    private String estado = "Pendiente";

    //Fecha del ultimo cambio de estado
    private LocalDateTime fechaEstado;

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "id_diagnostico", nullable = false, unique = true)
    private Diagnostico diagnostico;

    //Mecanico responsable, lo asigna el administrador
    @ManyToOne
    @JoinColumn(name = "id_mecanico")
    private Usuario mecanico;
}