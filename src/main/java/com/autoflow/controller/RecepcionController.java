package com.autoflow.controller;

import com.autoflow.domain.Reserva;
import com.autoflow.service.RecepcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.autoflow.domain.Recepcion;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.autoflow.domain.Vehiculo;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;

    //Pantalla principal de recepcion
    @GetMapping("/listado")
    public String inicio() {
        return "recepcion/inicio";
    }

    //Buscar reservas confirmadas
    @GetMapping("/con-reserva")
    public String buscarReservas(
            @RequestParam(
                    name = "busqueda",
                    defaultValue = ""
            ) String busqueda,
            Model model) {

        model.addAttribute("busqueda", busqueda);

        model.addAttribute(
                "reservas",
                recepcionService.buscarReservas(busqueda)
        );

        return "recepcion/conreserva";
    }

    //Mostrar los datos de una reserva seleccionada
    @GetMapping("/con-reserva/{idReserva}")
    public String detalleReserva(
            @PathVariable Integer idReserva,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Reserva reserva = recepcionService.validarLlegada(idReserva);
            model.addAttribute("reserva", reserva);
            return "recepcion/detalle";
        } catch (IllegalArgumentException e) {
            // Reserva cancelada o que ya fue recibida: se vuelve a la busqueda con el aviso
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recepcion/con-reserva";
        }
    }

    @GetMapping("/estado-inicial/{idReserva}")
    public String estadoInicial(
            @PathVariable Integer idReserva,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Reserva reserva = recepcionService.validarLlegada(idReserva);
            model.addAttribute("reserva", reserva);
            model.addAttribute("recepcion", new Recepcion());
            return "recepcion/estado";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recepcion/con-reserva";
        }
    }

    @PostMapping("/guardar-con-reserva/{idReserva}")
    public String guardarConReserva(
            @PathVariable Integer idReserva,
            @Valid @ModelAttribute("recepcion") Recepcion recepcion,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute("reserva",
                    recepcionService.obtenerReserva(idReserva));

            return "recepcion/estado";
        }

        try {
            recepcionService.registrarConReserva(idReserva, recepcion);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "La llegada del vehículo se registró correctamente");

            return "redirect:/recepcion/listado";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/recepcion/con-reserva/" + idReserva;
        }
    }

    @GetMapping("/sin-reserva")
    public String buscarVehiculos(
            @RequestParam(name = "busqueda", defaultValue = "") String busqueda,
            Model model) {

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("vehiculos",
                recepcionService.buscarVehiculos(busqueda));

        return "recepcion/sinreserva";
    }

    @GetMapping("/sin-reserva/{idVehiculo}")
    public String detalleVehiculo(
            @PathVariable Integer idVehiculo,
            Model model) {

        Vehiculo vehiculo = recepcionService.obtenerVehiculo(idVehiculo);

        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("recepcion", new Recepcion());

        return "recepcion/inicialsinreserva";
    }

    @PostMapping("/guardar-sin-reserva/{idVehiculo}")
    public String guardarSinReserva(
            @PathVariable Integer idVehiculo,
            @Valid @ModelAttribute("recepcion") Recepcion recepcion,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {

            model.addAttribute("vehiculo",
                    recepcionService.obtenerVehiculo(idVehiculo));

            return "recepcion/inicialsinreserva";
        }

        try {

            recepcionService.registrarSinReserva(idVehiculo, recepcion);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "La llegada del vehículo sin reserva se registró correctamente");

            return "redirect:/recepcion/listado";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());

            return "redirect:/recepcion/sin-reserva/" + idVehiculo;
        }
    }
}
