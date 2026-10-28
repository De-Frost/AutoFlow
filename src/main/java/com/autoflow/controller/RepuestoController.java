package com.autoflow.controller;

import com.autoflow.domain.Repuesto;
import com.autoflow.service.RepuestoService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/repuesto")
public class RepuestoController {

    private final RepuestoService repuestoService;
    private final MessageSource messageSource;

    @GetMapping("/listado")
    public String listado(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        var repuestos = repuestoService.buscar(buscar);
        model.addAttribute("repuestos", repuestos);
        model.addAttribute("totalRepuestos", repuestos.size());
        model.addAttribute("buscar", buscar);
        model.addAttribute("repuesto", new Repuesto()); // Objeto vacío para el modal de agregar
        return "repuesto/listado";
    }

    @GetMapping("/modificar/{idRepuesto}")
    public String modificar(@PathVariable("idRepuesto") Integer idRepuesto, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var repuesto = repuestoService.getRepuesto(idRepuesto);
        if (repuesto.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("repuesto.error01", null, locale));
            return "redirect:/repuesto/listado";
        }
        model.addAttribute("repuesto", repuesto.get());
        return "repuesto/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Repuesto repuesto, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String mensaje = "mensaje.actualizado";
        if (bindingResult.hasErrors()) {
            titulo = "error";
            mensaje = "repuesto.error04";
        } else {
            try {
                repuestoService.save(repuesto);
            } catch (DataIntegrityViolationException e) {
                titulo = "error";
                mensaje = "repuesto.error05";
            }
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/repuesto/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idRepuesto, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            repuestoService.delete(idRepuesto);
        } catch (IllegalArgumentException e) {
            titulo = "error"; //El repuesto no existe
            detalle = "repuesto.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; //El repuesto tiene datos asociados
            detalle = "repuesto.error02";
        } catch (Exception e) {
            titulo = "error"; //Cualquier otra excepcion
            detalle = "repuesto.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/repuesto/listado";
    }
}