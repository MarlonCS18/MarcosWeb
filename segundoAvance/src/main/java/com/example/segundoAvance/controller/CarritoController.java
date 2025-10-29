package com.example.segundoAvance.controller;

import com.example.segundoAvance.model.CarritoItem;
import com.example.segundoAvance.model.Producto;
import com.example.segundoAvance.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepository;

    // Método para obtener el carrito de la sesión
    private List<CarritoItem> getCarrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    // Mapeo para añadir un producto al carrito
    // En CarritoController.java

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Long id, @RequestParam int cantidad, HttpSession session) {
        List<CarritoItem> carrito = getCarrito(session);

        // Lógica para añadir o actualizar el producto (sin cambios)
        for (CarritoItem item : carrito) {
            if (item.getProducto().getId().equals(id)) {
                item.setCantidad(item.getCantidad() + cantidad);
                // ***** CAMBIO AQUÍ *****
                return "redirect:/productos"; // Redirige de vuelta al catálogo
            }
        }

        productoRepository.findById(id).ifPresent(producto -> {
            carrito.add(new CarritoItem(producto, cantidad));
        });

        // ***** Y CAMBIO AQUÍ *****
        return "redirect:/productos"; // Redirige de vuelta al catálogo
    }

    // Mapeo para mostrar la página del carrito
    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        List<CarritoItem> carrito = getCarrito(session);
        model.addAttribute("carrito", carrito);

        // Calcula el total de la compra
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        model.addAttribute("total", total);

        return "carrito";
    }

    // Mapeo para eliminar un producto del carrito
    @PostMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam Long id, HttpSession session) {
        List<CarritoItem> carrito = getCarrito(session);
        carrito.removeIf(item -> item.getProducto().getId().equals(id));
        return "redirect:/carrito";
    }
}