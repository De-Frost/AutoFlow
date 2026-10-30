package com.autoflow.controller;

import com.autoflow.domain.Recepcion;
import com.autoflow.domain.Reserva;
import com.autoflow.domain.Vehiculo;
import com.autoflow.service.RecepcionService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
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
@RequestMapping("/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;
    private final MessageSource messageSource;

    //Vehiculos recibidos en el taller
    @GetMapping("/listado")
    public String listado(Model model) {
        var recepciones = recepcionService.getRecepciones();
        model.addAttribute("recepciones", recepciones);
        model.addAttribute("totalRecepciones", recepciones.size());
        return "recepcion/listado";
    }

    //HU-R08: Buscar la reserva del vehiculo que llega
    @GetMapping("/conReserva")
    public String conReserva(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        var reservas = recepcionService.getReservasPorRecibir(buscar);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalReservas", reservas.size());
        model.addAttribute("buscar", buscar);
        return "recepcion/conReserva";
    }

    //HU-R09: Buscar el cliente y seleccionar el vehiculo que llega sin reserva
    @GetMapping("/sinReserva")
    public String sinReserva(@RequestParam(value = "buscar", required = false) String buscar,
            @RequestParam(value = "idCliente", required = false) Integer idCliente, Model model) {
        model.addAttribute("clientes", recepcionService.buscarClientes(buscar));
        model.addAttribute("buscar", buscar);
        if (idCliente != null) {
            var cliente = recepcionService.getCliente(idCliente);
            if (cliente.isPresent()) {
                model.addAttribute("cliente", cliente.get());
                model.addAttribute("vehiculos", recepcionService.getVehiculosCliente(idCliente));
            }
        }
        return "recepcion/sinReserva";
    }

    //HU-R10: Formulario del estado inicial del vehiculo (con reserva o sin reserva)
    @GetMapping("/nueva")
    public String nueva(@RequestParam(value = "idReserva", required = false) Integer idReserva,
            @RequestParam(value = "idVehiculo", required = false) Integer idVehiculo,
            Model model, RedirectAttributes redirectAttributes, Locale locale) {
        var recepcion = new Recepcion();
        if (idReserva != null) {
            var reserva = recepcionService.getReserva(idReserva);
            if (reserva.isEmpty() || !recepcionService.puedeRecibirse(reserva.get())) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("recepcion.error05", null, locale));
                return "redirect:/recepcion/conReserva";
            }
            recepcion.setFallaReportada(reserva.get().getMotivo()); //Se propone el motivo de la reserva
            cargarFormulario(model, recepcion, reserva.get(), reserva.get().getVehiculo(), false, false);
            return "recepcion/agrega";
        }
        if (idVehiculo != null) {
            var vehiculo = recepcionService.getVehiculo(idVehiculo);
            if (vehiculo.isPresent()) {
                cargarFormulario(model, recepcion, null, vehiculo.get(), false, false);
                return "recepcion/agrega";
            }
        }
        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("recepcion.error02", null, locale));
        return "redirect:/recepcion/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Recepcion recepcion, BindingResult bindingResult,
            @RequestParam(value = "idReserva", required = false) Integer idReserva,
            @RequestParam(value = "idVehiculo", required = false) Integer idVehiculo,
            @RequestParam(value = "conDanos", defaultValue = "false") boolean conDanos,
            @RequestParam(value = "conObjetos", defaultValue = "false") boolean conObjetos,
            Model model, RedirectAttributes redirectAttributes, Locale locale) {
        String mensaje = "recepcion.error01";
        if (!bindingResult.hasErrors()) {
            try {
                var guardada = recepcionService.guardar(recepcion, idReserva, idVehiculo, conDanos, conObjetos);
                String exito = (idReserva != null) ? "recepcion.exito.reserva" : "recepcion.exito";
                redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage(exito, null, locale));
                return "redirect:/recepcion/comprobante/" + guardada.getIdRecepcion();
            } catch (IllegalArgumentException e) {
                mensaje = e.getMessage();
            }
        }
        //Se vuelve a mostrar el formulario con los datos digitados y el motivo del rechazo
        Reserva reserva = null;
        Vehiculo vehiculo = null;
        if (idReserva != null) {
            reserva = recepcionService.getReserva(idReserva).orElse(null);
            if (reserva != null) {
                vehiculo = reserva.getVehiculo();
            }
        } else if (idVehiculo != null) {
            vehiculo = recepcionService.getVehiculo(idVehiculo).orElse(null);
        }
        if (vehiculo == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("recepcion.error02", null, locale));
            return "redirect:/recepcion/listado";
        }
        recepcion.setReserva(null);
        recepcion.setVehiculo(null);
        model.addAttribute("error", messageSource.getMessage(mensaje, null, locale));
        cargarFormulario(model, recepcion, reserva, vehiculo, conDanos, conObjetos);
        return "recepcion/agrega";
    }

    private void cargarFormulario(Model model, Recepcion recepcion, Reserva reserva, Vehiculo vehiculo,
            boolean conDanos, boolean conObjetos) {
        model.addAttribute("recepcion", recepcion);
        model.addAttribute("reserva", reserva);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("conDanos", conDanos);
        model.addAttribute("conObjetos", conObjetos);
    }

    //HU-R11: Comprobante de recepcion
    @GetMapping("/comprobante/{idRecepcion}")
    public String comprobante(@PathVariable("idRecepcion") Integer idRecepcion, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var recepcion = recepcionService.getRecepcion(idRecepcion);
        if (recepcion.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("recepcion.error07", null, locale));
            return "redirect:/recepcion/listado";
        }
        model.addAttribute("recepcion", recepcion.get());
        return "recepcion/comprobante";
    }
}
