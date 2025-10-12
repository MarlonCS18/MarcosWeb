package com.example.segundoAvance.controller;

import com.example.segundoAvance.model.Producto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductoController {

    private List<Producto> catalogoProductos = new ArrayList<>();
    private List<Producto> indexProductos = new ArrayList<>();

    public ProductoController() {
        Producto p1 = new Producto("Laptop Gamer", "Potente laptop con RTX 4060", 3500.0,
                "/img/laptop1.webp", "/img/laptop2.webp");
        Producto p2 = new Producto("Smartphone", "Pantalla AMOLED 120Hz", 1500.0,
                "/img/smartphone1.webp", "/img/smartphone2.webp");
        Producto p3 = new Producto("Auriculares", "Inalámbricos con cancelación de ruido", 500.0,
                "/img/audifonos1.webp", "/img/audifonos2.webp");
        Producto p4 = new Producto("Mouse Gamer", "Ergonómico con luces RGB personalizables", 200.0,
                "/img/mouse1.webp", "/img/mouse2.webp");

        catalogoProductos.add(p1);
        catalogoProductos.add(p2);
        catalogoProductos.add(p3);
        catalogoProductos.add(p4);

        indexProductos.add(p1);
        indexProductos.add(p2);
        indexProductos.add(p3);
        indexProductos.add(p4);
    }

    // Página principal
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", indexProductos);
        return "index";
    }

    // Página catálogo normal
    @GetMapping("/productos")
    public String mostrarProductos(Model model) {
        model.addAttribute("productos", catalogoProductos);
        return "productos";
    }

    // Página edición productos
    @GetMapping("/productos/editar")
    public String mostrarPaginaEdicion(Model model) {
        model.addAttribute("productos", catalogoProductos);
        return "editar-productos";
    }

    // Formulario añadir producto
    @GetMapping("/productos/add")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        return "add-producto";
    }

    // Procesar añadir producto
    @PostMapping("/productos/add")
    public String agregarProducto(@ModelAttribute Producto producto) {
        if (producto.getImagenHover() == null || producto.getImagenHover().isEmpty()) {
            producto.setImagenHover(null);
        }
        catalogoProductos.add(producto); // Solo añade al catálogo
        return "redirect:/productos";
    }

    // Formulario editar producto
    @GetMapping("/productos/edit/{index}")
    public String mostrarFormularioEdicion(@PathVariable int index, Model model) {
        if (index >= 0 && index < catalogoProductos.size()) {
            model.addAttribute("producto", catalogoProductos.get(index));
            model.addAttribute("index", index);
            return "edit-producto";
        }
        return "redirect:/productos";
    }

    // Procesar editar producto
    @PostMapping("/productos/edit/{index}")
    public String actualizarProducto(@PathVariable int index,
                                      @ModelAttribute Producto productoActualizado) {
        if (index >= 0 && index < catalogoProductos.size()) {
            if (productoActualizado.getImagenHover() == null || productoActualizado.getImagenHover().isEmpty()) {
                productoActualizado.setImagenHover(null);
            }
            catalogoProductos.set(index, productoActualizado);
        }
        return "redirect:/productos/editar";
    }

    // Eliminar producto
    @PostMapping("/productos/delete/{index}")
    public String eliminarProducto(@PathVariable int index) {
        if (index >= 0 && index < catalogoProductos.size()) {
            catalogoProductos.remove(index);
        }
        return "redirect:/productos/editar";
    }

    // API REST
    @ResponseBody
    @GetMapping("/productos/api")
    public List<Producto> getProductosApi() {
        return catalogoProductos;
    }

    @ResponseBody
    @PostMapping("/productos/api")
    public Producto addProductoApi(@RequestBody Producto producto) {
        if (producto.getImagenHover() == null || producto.getImagenHover().isEmpty()) {
            producto.setImagenHover(null);
        }
        catalogoProductos.add(producto);
        return producto;
    }

    @ResponseBody
    @PutMapping("/productos/api/{index}")
    public Producto updateProductoApi(@PathVariable int index, @RequestBody Producto producto) {
        if (index >= 0 && index < catalogoProductos.size()) {
            if (producto.getImagenHover() == null || producto.getImagenHover().isEmpty()) {
                producto.setImagenHover(null);
            }
            catalogoProductos.set(index, producto);
            return producto;
        }
        return null;
    }

    @ResponseBody
    @DeleteMapping("/productos/api/{index}")
    public String deleteProductoApi(@PathVariable int index) {
        if (index >= 0 && index < catalogoProductos.size()) {
            catalogoProductos.remove(index);
            return "Producto eliminado correctamente";
        }
        return "Producto no encontrado";
    }
}
