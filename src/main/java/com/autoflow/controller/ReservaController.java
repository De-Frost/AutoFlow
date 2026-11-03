package com.autoflow.controller;

import com.autoflow.domain.Reserva;
import com.autoflow.service.ReservaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;
    private final MessageSource messageSource;

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
        //Listas para el modal de nueva reserva (HU-R03)
        model.addAttribute("vehiculos", reservaService.getVehiculos());
        model.addAttribute("espacios", reservaService.getEspaciosActivos());
        return "reserva/listado";
    }

    //HU-R03: Registrar reserva
    @PostMapping("/guardar")
    public String guardar(@Valid Reserva reserva, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String mensaje = "reserva.exito";
        if (bindingResult.hasErrors()) {
            titulo = "error";
            mensaje = "reserva.error01";
        } else {
            try {
                reservaService.registrar(reserva);
            } catch (IllegalArgumentException e) {
                titulo = "error";
                mensaje = e.getMessage(); //La llave que puso el servicio
            }
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/reserva/listado";
    }

    //HU-R05: Cancelar reserva
    @PostMapping("/cancelar")
    public String cancelar(@RequestParam Integer idReserva,
            @RequestParam(value = "motivoCancelacion", required = false) String motivoCancelacion,
            RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String mensaje = "reserva.exitoCancelar";
        try {
            reservaService.cancelar(idReserva, motivoCancelacion);
        } catch (IllegalArgumentException e) {
            titulo = "error";
            mensaje = e.getMessage();
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/reserva/listado";
    }
}