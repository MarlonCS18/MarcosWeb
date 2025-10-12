package com.example.segundoAvance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private String imagenHover;
}