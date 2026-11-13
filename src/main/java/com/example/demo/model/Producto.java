package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;
    @Column(length = 255)
    private String imagen;
    @Column(length = 60)
    private String categoria;
    @Column(nullable = false)
    private boolean enOferta = false;
    @Column(precision = 12, scale = 2)
    private BigDecimal precioOferta;
    @Column(nullable = false)
    private Integer stock = 0;

    public Producto() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;}
    public BigDecimal getPrecio(){return precio;} public void setPrecio(BigDecimal precio){this.precio=precio;}
    public String getImagen(){return imagen;} public void setImagen(String imagen){this.imagen=imagen;}
    public String getCategoria(){return categoria;} public void setCategoria(String categoria){this.categoria=categoria;}
    public boolean isEnOferta(){return enOferta;} public void setEnOferta(boolean enOferta){this.enOferta=enOferta;}
    public BigDecimal getPrecioOferta(){return precioOferta;} public void setPrecioOferta(BigDecimal precioOferta){this.precioOferta=precioOferta;}
    public Integer getStock(){return stock;} public void setStock(Integer stock){this.stock=stock;}
}
