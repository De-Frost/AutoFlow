package com.autoflow.controller;

import com.autoflow.service.DiagnosticoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/diagnostico")
public class DiagnosticoController {
    private final DiagnosticoService servicio;

    @Getter @Setter
    public static class Formulario {
        @NotBlank(message = "Las fallas encontradas son obligatorias.")
        @Size(max = 10000, message = "Ingrese como máximo 10000 caracteres.")
        private String fallasEncontradas;
        @NotBlank(message = "Los trabajos necesarios son obligatorios.")
        @Size(max = 10000, message = "Ingrese como máximo 10000 caracteres.")
        private String trabajosNecesarios;
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(defaultValue = "") String busqueda, Model model) {
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("recepciones", servicio.pendientes(busqueda));
        return "diagnostico/listado";
    }

    @GetMapping("/registrar/{id}")
    public String formulario(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        var recepcion = servicio.recepcion(id);
        if (servicio.registrado(id)) {
            flash.addFlashAttribute("error", "Esta recepción ya tiene un diagnóstico registrado.");
            return "redirect:/diagnostico/listado";
        }
        model.addAttribute("recepcion", recepcion);
        if (!model.containsAttribute("formulario")) model.addAttribute("formulario", new Formulario());
        return "diagnostico/registrar";
    }

    @PostMapping("/registrar/{id}")
    public String guardar(@PathVariable Integer id,
            @Valid @ModelAttribute("formulario") Formulario formulario, BindingResult binding,
            Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) return formulario(id, model, flash);
        try {
            var diagnostico = servicio.registrar(id, formulario.getFallasEncontradas(), formulario.getTrabajosNecesarios());
            flash.addFlashAttribute("todoOk", "Diagnóstico registrado correctamente.");
            return "redirect:/orden/generar/" + diagnostico.getIdDiagnostico();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(id, model, flash);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "No se pudo guardar el diagnóstico. Compruebe si la recepción ya fue diagnosticada.");
            return formulario(id, model, flash);
        }
    }
}
