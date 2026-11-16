package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:4173"})
@Tag(name = "Pedidos", description = "Gestión de compras realizadas por los clientes")
public class PedidoController {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;

    public PedidoController(PedidoRepository p, UsuarioRepository u, ProductoRepository pr) {
        pedidos = p; usuarios = u; productos = pr;
    }

    public record Linea(Long productoId, String nombre, Integer cantidad, BigDecimal precio) {}
    public record PedidoIn(Long usuarioId, BigDecimal total, String estado, List<Linea> productos) {}
    public record LineaOut(Long id, Long productoId, String nombre, Integer cantidad, BigDecimal precio) {}
    public record PedidoOut(Long id, Long usuarioId, String usuarioEmail, LocalDate fecha,
                            BigDecimal total, String estado, List<LineaOut> productos) {}

    private PedidoOut out(Pedido p) {
        return new PedidoOut(p.getId(),
            p.getUsuario() == null ? null : p.getUsuario().getId(),
            p.getUsuario() == null ? null : p.getUsuario().getEmail(),
            p.getFecha(), p.getTotal(), p.getEstado(),
            p.getProductos().stream().map(d -> new LineaOut(d.getId(),
                d.getProducto() == null ? null : d.getProducto().getId(),
                d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario())).toList());
    }

    private ResponseEntity<?> validate(PedidoIn in) {
        if (in == null || in.usuarioId() == null || usuarios.findById(in.usuarioId()).isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Usuario no encontrado"));
        if (in.productos() == null || in.productos().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "El pedido debe contener al menos un detalle"));
        for (Linea l : in.productos()) {
            if (l.cantidad() == null || l.cantidad() < 1 || l.precio() == null || l.precio().signum() < 0 || l.nombre() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Cada detalle requiere nombre, cantidad válida y precio"));
        }
        return null;
    }

    private void fill(Pedido p, PedidoIn in) {
        Usuario u = usuarios.findById(in.usuarioId()).orElseThrow();
        p.setUsuario(u);
        p.setTotal(in.total() == null ? in.productos().stream()
            .map(l -> l.precio().multiply(BigDecimal.valueOf(l.cantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add) : in.total());
        p.setEstado(in.estado() == null || in.estado().isBlank() ? "Pendiente" : in.estado());
        if (p.getFecha() == null) p.setFecha(LocalDate.now());
        p.getProductos().clear();
        for (Linea l : in.productos()) {
            PedidoDetalle d = new PedidoDetalle();
            d.setPedido(p);
            d.setProducto(l.productoId() == null ? null : productos.findById(l.productoId()).orElse(null));
            d.setNombreProducto(l.nombre());
            d.setCantidad(l.cantidad());
            d.setPrecioUnitario(l.precio());
            p.getProductos().add(d);
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar pedidos")
    public List<PedidoOut> all() { return pedidos.findAll().stream().map(this::out).toList(); }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtener pedido por ID")
    public ResponseEntity<PedidoOut> one(@PathVariable Long id) {
        return pedidos.findById(id).map(p -> ResponseEntity.ok(out(p))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Crear pedido", description = "Crea un pedido junto con sus detalles. El frontend Yakarta ya utiliza este formato.")
    public ResponseEntity<?> create(@RequestBody PedidoIn in) {
        ResponseEntity<?> error = validate(in);
        if (error != null) return error;
        Pedido p = new Pedido();
        p.setFecha(LocalDate.now());
        fill(p, in);
        return ResponseEntity.status(201).body(out(pedidos.save(p)));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Actualizar pedido y sus detalles")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PedidoIn in) {
        ResponseEntity<?> error = validate(in);
        if (error != null) return error;
        return pedidos.findById(id).map(p -> {
            fill(p, in);
            return ResponseEntity.ok(out(pedidos.save(p)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido y sus detalles")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!pedidos.existsById(id)) return ResponseEntity.notFound().build();
        pedidos.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
