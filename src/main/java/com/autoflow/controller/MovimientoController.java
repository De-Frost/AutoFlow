package com.autoflow.controller;

import com.autoflow.service.MovimientoService;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/movimiento")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final MessageSource messageSource;

    //Pantalla Repuestos - Movimientos
    @GetMapping("/listado")
    public String listado(@RequestParam(value = "idOrden", required = false) Integer idOrden, Model model) {
        model.addAttribute("ordenes", movimientoService.getOrdenesAbiertas());
        model.addAttribute("repuestos", movimientoService.getRepuestosActivos());
        model.addAttribute("movimientos", movimientoService.getMovimientos());
        model.addAttribute("idOrden", idOrden);
        model.addAttribute("usados", idOrden != null ? movimientoService.getRepuestosDeOrden(idOrden) : List.of());
        return "movimiento/listado";
    }

    //Boton Registrar consumo
    @PostMapping("/consumo")
    public String consumo(@RequestParam Integer idOrden, @RequestParam Integer idRepuesto,
            @RequestParam(required = false) Integer cantidad,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            var m = movimientoService.registrarConsumo(idOrden, idRepuesto, cantidad);
            //Mensaje con el stock anterior y el actualizado
            Object[] datos = {m.getCantidad(), m.getRepuesto().getCodigo(), m.getRepuesto().getNombre(),
                m.getStockAnterior(), m.getStockNuevo()};
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("movimiento.exito", datos, locale));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
        }
        redirectAttributes.addAttribute("idOrden", idOrden);
        return "redirect:/movimiento/listado";
    }
}