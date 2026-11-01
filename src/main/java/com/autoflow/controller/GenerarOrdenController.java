package com.autoflow.controller;

import com.autoflow.service.GenerarOrdenService;
import com.autoflow.service.OrdenServicioService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class GenerarOrdenController {
    private final GenerarOrdenService servicio;
    private final OrdenServicioService ordenes;

    @Getter @Setter
    public static class Formulario {
        private List<String> descripcion = new ArrayList<>();
        private List<Integer> cantidad = new ArrayList<>();
        private List<BigDecimal> costo = new ArrayList<>();
    }

    @GetMapping("/orden/nueva")
    public String pendientes(Model model) {
        model.addAttribute("diagnosticos", servicio.pendientes());
        return "orden/diagnosticos";
    }

    @GetMapping("/orden/generar/{id}")
    public String formulario(@PathVariable Integer id, Model model) {
        model.addAttribute("diagnostico", servicio.diagnostico(id));
        if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new Formulario());
        return "orden/generar";
    }

    @PostMapping("/orden/generar/{id}")
    public String generar(@PathVariable Integer id, @ModelAttribute Formulario formulario,
            org.springframework.validation.BindingResult binding, Model model, RedirectAttributes flash) {
        try {
            if (binding.hasErrors() || formulario.getDescripcion().size() != formulario.getCantidad().size()
                    || formulario.getDescripcion().size() != formulario.getCosto().size()) {
                throw new IllegalArgumentException("Revise las cantidades y los costos de los servicios.");
            }
            var lineas = new ArrayList<GenerarOrdenService.Linea>();
            for (int i = 0; i < formulario.getDescripcion().size(); i++) {
                lineas.add(new GenerarOrdenService.Linea(formulario.getDescripcion().get(i),
                        formulario.getCantidad().get(i), formulario.getCosto().get(i)));
            }
            var orden = servicio.generar(id, lineas);
            flash.addFlashAttribute("todoOk", "Orden de servicio generada correctamente.");
            return "redirect:/orden/" + orden.getIdOrden() + "/servicios";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(id, model);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "No se pudo generar la orden. Compruebe si el diagnóstico ya tiene una orden.");
            return formulario(id, model);
        }
    }

    @GetMapping("/orden/{id}/servicios")
    public String resumen(@PathVariable Integer id, Model model) {
        model.addAttribute("orden", ordenes.obtener(id));
        var detalles = servicio.servicios(id);
        model.addAttribute("servicios", detalles);
        model.addAttribute("subtotal", detalles.stream().map(d -> d.getCosto().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "orden/servicios";
    }
}
