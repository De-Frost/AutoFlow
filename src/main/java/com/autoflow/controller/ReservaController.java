package com.autoflow.controller;

import com.autoflow.service.ReservaService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;
    private final MessageSource messageSource; //Se usa en HU-R03 y HU-R05

    @GetMapping("/listado")
    public String listado(
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "placa", required = false) String placa,
            Model model) {
        var reservas = reservaService.buscar(fecha, estado, placa);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalReservas", reservas.size());
        //Se devuelven los filtros para que el formulario los siga mostrando
        model.addAttribute("fecha", fecha);
        model.addAttribute("estado", estado);
        model.addAttribute("placa", placa);
        return "reserva/listado";
    }
}