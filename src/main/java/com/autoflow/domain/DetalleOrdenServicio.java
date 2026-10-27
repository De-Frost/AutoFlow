package com.autoflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_orden_servicio")
public class DetalleOrdenServicio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_servicio")
    private Integer idDetalleServicio;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 150)
    private String descripcion;

    @NotNull
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad = 1;

    @Column(precision = 12, scale = 2, nullable = false)
    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
    private BigDecimal costo;

    @ManyToOne
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenServicio orden;
}