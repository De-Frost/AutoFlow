package com.autoflow.controller;

import com.autoflow.service.OrdenServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class OrdenServicioController {
    private final OrdenServicioService servicio;

    @GetMapping({"/orden/listado", "/orden/seguimiento", "/mecanico/listado"})
    public String listado(Model model) {
        model.addAttribute("ordenes", servicio.listar());
        return "orden/listado";
    }

    @GetMapping("/orden/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        model.addAttribute("orden", servicio.obtener(id));
        return "orden/detalle";
    }

    @GetMapping("/orden/{id}/mecanicos")
    public String mecanicos(@PathVariable Integer id, Model model) {
        model.addAttribute("orden", servicio.obtener(id));
        model.addAttribute("mecanicos", servicio.mecanicos());
        return "orden/mecanicos";
    }

    @PostMapping("/orden/{id}/asignar")
    public String asignar(@PathVariable Integer id, @RequestParam Integer idMecanico,
            RedirectAttributes flash) {
        try {
            servicio.asignar(id, idMecanico);
            return "redirect:/orden/" + id + "/confirmacion";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orden/" + id + "/mecanicos";
        }
    }

    @GetMapping("/orden/{id}/confirmacion")
    public String confirmacion(@PathVariable Integer id, Model model) {
        var orden = servicio.obtener(id);
        if (orden.getMecanico() == null) return "redirect:/orden/" + id + "/mecanicos";
        model.addAttribute("orden", orden);
        model.addAttribute("carga", servicio.carga(orden.getMecanico()));
        return "orden/confirmacion";
    }

    @GetMapping("/orden/{id}/estado")
    public String estado(@PathVariable Integer id, Model model) {
        model.addAttribute("orden", servicio.obtener(id));
        model.addAttribute("estados", OrdenServicioService.ESTADOS);
        return "orden/estado";
    }

    @PostMapping("/orden/{id}/estado")
    public String actualizarEstado(@PathVariable Integer id, @RequestParam String estado,
            RedirectAttributes flash) {
        try {
            servicio.actualizarEstado(id, estado);
            flash.addFlashAttribute("todoOk", "Estado de la orden actualizado correctamente.");
            return "redirect:/orden/" + id;
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orden/" + id + "/estado";
        }
    }
}
