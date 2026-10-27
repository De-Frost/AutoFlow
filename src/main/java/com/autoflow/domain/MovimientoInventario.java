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
@Table(name = "movimiento_inventario")
public class MovimientoInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    //ENTRADA o SALIDA
    @Column(nullable = false, length = 10)
    private String tipo;

    @NotNull
    @Min(value = 1)
    private Integer cantidad;

    //Existencias antes y despues del movimiento
    private Integer stockAnterior;
    private Integer stockNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_repuesto", nullable = false)
    private Repuesto repuesto;

    //Solo las salidas estan ligadas a una orden de servicio
    @ManyToOne
    @JoinColumn(name = "id_orden")
    private OrdenServicio orden;

    //Usuario que registro el movimiento
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}