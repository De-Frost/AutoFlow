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
@Table(name = "factura")
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    //Una orden solo puede tener una factura
    @OneToOne
    @JoinColumn(name = "id_orden", nullable = false, unique = true)
    private OrdenServicio orden;

    //Recepcionista que genero la factura
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}