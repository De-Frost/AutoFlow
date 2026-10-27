package com.autoflow.repository;

import com.autoflow.domain.Reserva;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    public List<Reserva> findByFechaAndEstadoNotOrderByHoraInicioAsc(LocalDate fecha, String estado);

    public List<Reserva> findByEstadoOrderByFechaAscHoraInicioAsc(String estado);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r "
            + "WHERE r.espacio.idEspacio = :idEspacio AND r.fecha = :fecha AND r.estado <> 'Cancelada' "
            + "AND r.horaInicio < :horaFin AND r.horaFin > :horaInicio AND r.idReserva <> :idReserva")
    public boolean existeConflicto(@Param("idEspacio") Integer idEspacio, @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin,
            @Param("idReserva") Integer idReserva);
}