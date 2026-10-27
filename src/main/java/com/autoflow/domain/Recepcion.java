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
@Table(name = "recepcion")
public class Recepcion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Integer idRecepcion;

    @Column(nullable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer kilometraje;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El nivel de combustible es obligatorio")
    @Size(max = 20)
    private String nivelCombustible;

    @Column(length = 255)
    @Size(max = 255)
    private String danosVisibles;

    @Column(length = 255)
    @Size(max = 255)
    private String objetosPersonales;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "La falla reportada es obligatoria")
    @Size(max = 255)
    private String fallaReportada;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    //Puede venir de una reserva o llegar sin reserva
    @OneToOne
    @JoinColumn(name = "id_reserva", unique = true)
    private Reserva reserva;
}