package com.autoflow.controller;

import com.autoflow.domain.Espacio;
import com.autoflow.domain.Reserva;
import com.autoflow.service.ReservaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    //HU-R06: Listado de reservas filtrado por fecha, estado y placa
    @GetMapping("/listado")
    public String listado(@RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "placa", required = false) String placa, Model model) {
        var reservas = reservaService.buscar(fecha, estado, placa);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalReservas", reservas.size());
        model.addAttribute("fecha", fecha);
        model.addAttribute("estado", estado);
        model.addAttribute("placa", placa);
        return "reserva/listado";
    }

    //HU-R02: Cuadricula de disponibilidad de espacios por fecha
    @GetMapping("/disponibilidad")
    public String disponibilidad(@RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        model.addAttribute("fecha", fecha);
        model.addAttribute("espacios", reservaService.getEspacios(false));
        model.addAttribute("franjas", reservaService.getFranjas());
        model.addAttribute("franjasPasadas", reservaService.getFranjasPasadas(fecha));
        model.addAttribute("ocupacion", reservaService.getOcupacion(fecha));
        return "reserva/disponibilidad";
    }

    //HU-R03: Formulario de nueva reserva (puede venir con el espacio y la hora de la cuadricula)
    @GetMapping("/nueva")
    public String nueva(@RequestParam(value = "idEspacio", required = false) Integer idEspacio,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            Model model) {
        var reserva = new Reserva();
        reserva.setFecha(fecha);
        if (hora != null) {
            reserva.setHoraInicio(hora);
            reserva.setHoraFin(hora.plusHours(1));
        }
        if (idEspacio != null) {
            var espacio = new Espacio();
            espacio.setIdEspacio(idEspacio);
            reserva.setEspacio(espacio);
        }
        cargarFormulario(model, reserva);
        return "reserva/agrega";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Reserva reserva, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        String mensaje = "reserva.error02";
        if (!bindingResult.hasErrors()) {
            try {
                reservaService.registrar(reserva);
                redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("reserva.exito",
                        new Object[]{String.valueOf(reserva.getIdReserva())}, locale));
                return "redirect:/reserva/disponibilidad?fecha=" + reserva.getFecha();
            } catch (IllegalArgumentException e) {
                mensaje = e.getMessage();
            }
        }
        //Se vuelve a mostrar el formulario con los datos digitados y el motivo del rechazo
        model.addAttribute("error", messageSource.getMessage(mensaje, null, locale));
        cargarFormulario(model, reserva);
        return "reserva/agrega";
    }

    private void cargarFormulario(Model model, Reserva reserva) {
        model.addAttribute("reserva", reserva);
        model.addAttribute("clientes", reservaService.getClientes());
        model.addAttribute("vehiculos", reservaService.getVehiculos());
        model.addAttribute("espacios", reservaService.getEspacios(true));
    }

    //Detalle de la reserva (desde la cuadricula o el listado), con las opciones de modificar y cancelar
    @GetMapping("/detalle/{idReserva}")
    public String detalle(@PathVariable("idReserva") Integer idReserva, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var reserva = reservaService.getReserva(idReserva);
        if (reserva.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("reserva.error01", null, locale));
            return "redirect:/reserva/listado";
        }
        model.addAttribute("reserva", reserva.get());
        model.addAttribute("modificable", reservaService.esModificable(reserva.get()));
        model.addAttribute("recibida", reservaService.tieneRecepcion(idReserva));
        return "reserva/detalle";
    }

    //HU-R04: Modificar reserva
    @GetMapping("/modificar/{idReserva}")
    public String modificar(@PathVariable("idReserva") Integer idReserva, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var reserva = reservaService.getReserva(idReserva);
        if (reserva.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("reserva.error01", null, locale));
            return "redirect:/reserva/listado";
        }
        if (!reservaService.esModificable(reserva.get())) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("reserva.error08", null, locale));
            return "redirect:/reserva/detalle/" + idReserva;
        }
        model.addAttribute("reserva", reserva.get());
        model.addAttribute("espacios", reservaService.getEspacios(true));
        return "reserva/modifica";
    }

    @PostMapping("/actualizar")
    public String actualizar(@Valid Reserva reserva, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("reserva.error02", null, locale));
            return "redirect:/reserva/modificar/" + reserva.getIdReserva();
        }
        try {
            var actualizada = reservaService.modificar(reserva);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("reserva.modificada", null, locale));
            return "redirect:/reserva/disponibilidad?fecha=" + actualizada.getFecha();
        } catch (IllegalArgumentException e) {
            //Se conservan los datos originales y se muestra el motivo del rechazo
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/reserva/modificar/" + reserva.getIdReserva();
        }
    }

    //HU-R05: Cancelar reserva
    @PostMapping("/cancelar")
    public String cancelar(@RequestParam Integer idReserva,
            @RequestParam(value = "motivoCancelacion", required = false) String motivoCancelacion,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            var cancelada = reservaService.cancelar(idReserva, motivoCancelacion);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("reserva.cancelada", null, locale));
            return "redirect:/reserva/disponibilidad?fecha=" + cancelada.getFecha();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/reserva/detalle/" + idReserva;
        }
    }
}
