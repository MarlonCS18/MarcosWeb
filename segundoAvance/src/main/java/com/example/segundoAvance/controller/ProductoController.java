package com.example.segundoAvance.controller;

import com.example.segundoAvance.model.Producto;
import com.example.segundoAvance.repository.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Añade la URI actual a cada petición para resaltar el enlace activo en la navegación.
     */
    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Muestra la página de inicio con los 4 productos más recientes.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoRepository.findTop4ByOrderByIdDesc());
        return "index";
    }

    /**
     * Muestra la página "Nosotros".
     */
    @GetMapping("/nosotros")
    public String mostrarPaginaNosotros() {
        return "nosotros";
    }

    /**
     * Muestra la página del catálogo, con la opción de filtrar por categoría.
     */
    @GetMapping("/productos")
    public String mostrarProductos(@RequestParam(required = false) String categoria, Model model) {
        List<Producto> productos;
        if (categoria != null && !categoria.isEmpty()) {
            productos = productoRepository.findByCategoria(categoria);
        } else {
            productos = productoRepository.findAll();
        }
        model.addAttribute("productos", productos);
        return "productos";
    }

    /**
     * Muestra la página de detalles de un producto específico.
     */
    @GetMapping("/producto/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isPresent()) {
            model.addAttribute("producto", productoOptional.get());
            return "producto-detalle";
        }
        return "redirect:/productos";
    }
    
    /**
     * Muestra la página de contacto.
     */
    @GetMapping("/contacto")
    public String mostrarPaginaContacto() {
        return "contacto";
    }

    /**
     * Muestra la página de login.
     */
    @GetMapping("/login")
    public String mostrarPaginaLogin() {
        return "login";
    }
}