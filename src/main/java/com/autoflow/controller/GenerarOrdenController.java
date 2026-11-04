package com.autoflow.controller;

import com.autoflow.domain.DetalleOrdenServicio;
import com.autoflow.service.GenerarOrdenService;
import com.autoflow.service.OrdenServicioService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class GenerarOrdenController {
    private final GenerarOrdenService servicio;
    private final OrdenServicioService ordenes;
    private final MessageSource messageSource;

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
    public String formulario(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            model.addAttribute("diagnostico", servicio.diagnostico(id));
            if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new Formulario());
            return "orden/generar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/nueva";
        }
    }

    @PostMapping("/orden/generar/{id}")
    public String generar(@PathVariable Integer id, @ModelAttribute Formulario formulario,
            BindingResult binding, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            if (binding.hasErrors() || formulario.getDescripcion().size() != formulario.getCantidad().size()
                    || formulario.getDescripcion().size() != formulario.getCosto().size()) {
                throw new IllegalArgumentException("generar.error06");
            }
            var lineas = new ArrayList<DetalleOrdenServicio>();
            for (int i = 0; i < formulario.getDescripcion().size(); i++) {
                var linea = new DetalleOrdenServicio();
                linea.setDescripcion(formulario.getDescripcion().get(i));
                linea.setCantidad(formulario.getCantidad().get(i));
                linea.setCosto(formulario.getCosto().get(i));
                lineas.add(linea);
            }
            var orden = servicio.generar(id, lineas);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("generar.exito", null, locale));
            return "redirect:/orden/" + orden.getIdOrden() + "/servicios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return formulario(id, model, redirectAttributes, locale);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", messageSource.getMessage("generar.error07", null, locale));
            return formulario(id, model, redirectAttributes, locale);
        }
    }

    @GetMapping("/orden/{id}/servicios")
    public String resumen(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            model.addAttribute("orden", ordenes.obtener(id));
            var detalles = servicio.servicios(id);
            model.addAttribute("servicios", detalles);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (var detalle : detalles) {
                subtotal = subtotal.add(detalle.getCosto().multiply(BigDecimal.valueOf(detalle.getCantidad())));
            }
            model.addAttribute("subtotal", subtotal);
            return "orden/servicios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/listado";
        }
    }
}
