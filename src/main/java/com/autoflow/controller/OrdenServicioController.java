package com.autoflow.controller;

import com.autoflow.service.OrdenServicioService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class OrdenServicioController {
    private final OrdenServicioService servicio;
    private final MessageSource messageSource;

    @GetMapping({"/orden/listado", "/orden/seguimiento", "/mecanico/listado"})
    public String listado(Model model) {
        model.addAttribute("ordenes", servicio.listar());
        return "orden/listado";
    }

    @GetMapping("/orden/{id}")
    public String detalle(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            model.addAttribute("orden", servicio.obtener(id));
            return "orden/detalle";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/listado";
        }
    }

    @GetMapping("/orden/{id}/mecanicos")
    public String mecanicos(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            model.addAttribute("orden", servicio.obtener(id));
            model.addAttribute("mecanicos", servicio.mecanicos());
            return "orden/mecanicos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/listado";
        }
    }

    @PostMapping("/orden/{id}/asignar")
    public String asignar(@PathVariable Integer id, @RequestParam Integer idMecanico,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            servicio.asignar(id, idMecanico);
            return "redirect:/orden/" + id + "/confirmacion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/" + id + "/mecanicos";
        }
    }

    @GetMapping("/orden/{id}/confirmacion")
    public String confirmacion(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            var orden = servicio.obtener(id);
            if (orden.getMecanico() == null) return "redirect:/orden/" + id + "/mecanicos";
            model.addAttribute("orden", orden);
            model.addAttribute("carga", servicio.carga(orden.getMecanico()));
            return "orden/confirmacion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/listado";
        }
    }

    @GetMapping("/orden/{id}/estado")
    public String estado(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            model.addAttribute("orden", servicio.obtener(id));
            model.addAttribute("estados", OrdenServicioService.ESTADOS);
            return "orden/estado";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/listado";
        }
    }

    @PostMapping("/orden/{id}/estado")
    public String actualizarEstado(@PathVariable Integer id, @RequestParam String estado,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            servicio.actualizarEstado(id, estado);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("orden.exitoEstado", null, locale));
            return "redirect:/orden/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/orden/" + id + "/estado";
        }
    }
}
