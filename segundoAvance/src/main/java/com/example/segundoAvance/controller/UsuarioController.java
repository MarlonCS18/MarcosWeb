package com.example.segundoAvance.controller;

import com.example.segundoAvance.model.Usuario;
import com.example.segundoAvance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Muestra el formulario de registro para nuevos usuarios.
     */
    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    /**
     * Procesa los datos del formulario de registro, guarda el nuevo usuario
     * con el rol "USER" y redirige a la página de login.
     */
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("ROLE_USER");
        usuarioRepository.save(usuario);
        return "redirect:/login?registro_exitoso";
    }

    /**
     * Muestra la página "Mi Perfil" con los datos del usuario que ha iniciado sesión.
     */
    @GetMapping("/mi-cuenta")
    public String verMiCuenta(Model model, Principal principal) {
        // 'Principal' contiene la información del usuario autenticado.
        // Usamos su nombre (que es el email) para buscarlo en la base de datos.
        usuarioRepository.findByEmail(principal.getName()).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        return "mi-cuenta";
    }

    /**
     * Muestra el formulario para editar el perfil del usuario actual.
     */
    @GetMapping("/editar-perfil")
    public String mostrarFormularioEditarPerfil(Model model, Principal principal) {
        usuarioRepository.findByEmail(principal.getName()).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        return "editar-perfil";
    }

    /**
     * Procesa los datos del formulario de edición y actualiza el perfil del usuario.
     */
    @PostMapping("/editar-perfil")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado, Principal principal) {
        usuarioRepository.findByEmail(principal.getName()).ifPresent(usuario -> {
            // Actualizamos solo los campos permitidos
            usuario.setNombreCompleto(usuarioActualizado.getNombreCompleto());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setDireccion(usuarioActualizado.getDireccion());
            
            usuarioRepository.save(usuario);
        });
        
        return "redirect:/mi-cuenta";
    }
}