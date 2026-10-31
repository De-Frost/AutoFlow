package com.autoflow.controller;

import com.autoflow.domain.OrdenServicio;
import com.autoflow.service.FacturaService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/factura")
public class FacturaController {

    private final FacturaService facturaService;
    private final MessageSource messageSource;

    //Pantalla Facturacion
    @GetMapping("/pendientes")
    public String pendientes(Model model) {
        model.addAttribute("pendientes", facturaService.getPendientes());
        model.addAttribute("facturas", facturaService.getFacturas());
        return "factura/pendientes";
    }

    //Vista previa de la factura
    @GetMapping("/generar/{idOrden}")
    public String vistaPrevia(@PathVariable Integer idOrden, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        try {
            OrdenServicio orden = facturaService.getOrdenFacturable(idOrden);
            cargarDetalle(model, orden);
            // Sin atributo "factura": la vista muestra la vista previa con el botón Generar
            return "factura/factura";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/factura/pendientes";
        }
    }

    //Boton para Generar Factura
    @PostMapping("/generar")
    public String generar(@RequestParam Integer idOrden, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            var factura = facturaService.generar(idOrden);
            Object[] datos = {factura.getIdFactura(), factura.getOrden().getIdOrden()};
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("factura.exito", datos, locale));
            return "redirect:/factura/ver/" + factura.getIdFactura();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/factura/pendientes";
        }
    }

    //Ver una factura ya emitida
    @GetMapping("/ver/{idFactura}")
    public String ver(@PathVariable Integer idFactura, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var factura = facturaService.getFactura(idFactura).orElse(null);
        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("factura.error04", null, locale));
            return "redirect:/factura/pendientes";
        }
        cargarDetalle(model, factura.getOrden());
        model.addAttribute("factura", factura);
        return "factura/factura";
    }
    
    private void cargarDetalle(Model model, OrdenServicio orden) {
        model.addAttribute("orden", orden);
        model.addAttribute("servicios", facturaService.getServicios(orden.getIdOrden()));
        model.addAttribute("repuestos", facturaService.getRepuestos(orden.getIdOrden()));
        model.addAttribute("total", facturaService.calcularTotal(orden.getIdOrden()));
    }
}