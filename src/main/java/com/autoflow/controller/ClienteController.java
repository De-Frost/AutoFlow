package com.autoflow.controller;

import com.autoflow.domain.Cliente;
import com.autoflow.service.ClienteService;
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
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final MessageSource messageSource;

    @GetMapping("/listado")
    public String listado(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        var clientes = clienteService.buscar(buscar);
        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", clientes.size());
        model.addAttribute("buscar", buscar);
        model.addAttribute("cliente", new Cliente()); //Objeto vacio para el modal de registrar
        return "cliente/listado";
    }

    @GetMapping("/modificar/{idCliente}")
    public String modificar(@PathVariable("idCliente") Integer idCliente, Model model,
            RedirectAttributes redirectAttributes, Locale locale) {
        var cliente = clienteService.getCliente(idCliente);
        if (cliente.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("cliente.error01", null, locale));
            return "redirect:/cliente/listado";
        }
        model.addAttribute("cliente", cliente.get());
        return "cliente/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Cliente cliente, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String mensaje = "mensaje.actualizado";
        if (bindingResult.hasErrors()) {
            titulo = "error";
            mensaje = "cliente.error04";
        } else {
            try {
                clienteService.save(cliente);
            } catch (DataIntegrityViolationException e) {
                titulo = "error"; //La identificacion ya existe
                mensaje = "cliente.error05";
            } catch (IllegalArgumentException e) {
                titulo = "error"; //El cliente que se editaba ya no existe
                mensaje = "cliente.error01";
            }
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/cliente/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idCliente, RedirectAttributes redirectAttributes, Locale locale) {
        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            clienteService.delete(idCliente);
        } catch (IllegalArgumentException e) {
            titulo = "error"; //El cliente no existe
            detalle = "cliente.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; //El cliente tiene datos asociados
            detalle = "cliente.error02";
        } catch (Exception e) {
            titulo = "error"; //Cualquier otra excepcion
            detalle = "cliente.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, locale));
        return "redirect:/cliente/listado";
    }
}
