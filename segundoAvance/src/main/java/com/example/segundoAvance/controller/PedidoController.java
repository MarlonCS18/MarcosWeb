package com.example.segundoAvance.controller;

import com.example.segundoAvance.model.CarritoItem;
import com.example.segundoAvance.model.Pedido;
import com.example.segundoAvance.model.Producto;
import com.example.segundoAvance.model.Usuario;
import com.example.segundoAvance.repository.PedidoRepository;
import com.example.segundoAvance.repository.ProductoRepository;
import com.example.segundoAvance.repository.UsuarioRepository;
import com.example.segundoAvance.service.PdfService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PedidoController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private PdfService pdfService;

    /**
     * Muestra la página para finalizar la compra, cargando los datos del usuario
     * y el resumen del carrito.
     */
    @GetMapping("/finalizar-compra")
    public String mostrarFormularioFinalizarCompra(Model model, Principal principal, HttpSession session) {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        double total = (carrito != null) ? carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum() : 0.0;
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        
        return "finalizar-compra";
    }

    /**
     * Procesa la compra: actualiza datos, reduce stock, crea el pedido y limpia el carrito.
     */
    @PostMapping("/procesar-compra")
    @Transactional
    public String procesarCompra(@ModelAttribute Usuario usuarioConDatosDeEnvio, HttpSession session, Principal principal) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/productos";
        }
        
        Usuario usuarioActual = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        usuarioActual.setTelefono(usuarioConDatosDeEnvio.getTelefono());
        usuarioActual.setDireccion(usuarioConDatosDeEnvio.getDireccion());
        usuarioRepository.save(usuarioActual);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuarioActual);
        pedido.setFechaCreacion(LocalDateTime.now());
        
        for (CarritoItem item : carrito) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProducto().getNombre()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
            pedido.getProductos().add(producto);
        }
        
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        pedido.setTotal(total);
        pedidoRepository.save(pedido);

        session.removeAttribute("carrito");
        
        return "redirect:/compra-exitosa";
    }
    
    /**
     * Muestra la página de confirmación después de una compra exitosa.
     */
    @GetMapping("/compra-exitosa")
    public String mostrarCompraExitosa() {
        return "compra-exitosa";
    }

    /**
     * Muestra el historial de pedidos del usuario logueado.
     */
    @GetMapping("/mis-pedidos")
    public String verMisPedidos(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        model.addAttribute("pedidos", usuario.getPedidos());
        
        return "mis-pedidos";
    }
    
    /**
     * Genera y descarga la boleta de un pedido en formato PDF.
     */
    @GetMapping("/pedido/pdf/{id}")
    public ResponseEntity<InputStreamResource> generarBoletaPdf(@PathVariable Long id, Principal principal) throws IOException, DocumentException, AccessDeniedException {
        
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getUsuario().getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("No tienes permiso para ver esta boleta.");
        }

        ByteArrayInputStream bis = pdfService.generarBoletaPdf(pedido);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=boleta_pedido_" + id + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}