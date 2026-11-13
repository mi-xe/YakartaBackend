package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    @Column(nullable = false, length = 30)
    private String estado = "Pendiente";
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> productos = new ArrayList<>();

    public Pedido() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario usuario){this.usuario=usuario;}
    public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate fecha){this.fecha=fecha;}
    public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal total){this.total=total;}
    public String getEstado(){return estado;} public void setEstado(String estado){this.estado=estado;}
    public List<PedidoDetalle> getProductos(){return productos;} public void setProductos(List<PedidoDetalle> productos){this.productos=productos;}
}
