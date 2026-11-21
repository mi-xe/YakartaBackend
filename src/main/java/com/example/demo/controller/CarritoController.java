package com.example.demo.controller;

import com.example.demo.model.CarritoItem;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.service.SessionAuth;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private final CarritoItemRepository carrito;
    private final ProductoRepository productos;
    private final SessionAuth auth;
    public CarritoController(CarritoItemRepository carrito, ProductoRepository productos, SessionAuth auth) {
        this.carrito = carrito; this.productos = productos; this.auth = auth;
    }
    public record CartInput(Long productoId, Integer cantidad) {}
    public record CartLine(Long id, Long productoId, String nombre, BigDecimal precio, String imagen,
                           String categoria, boolean enOferta, BigDecimal precioOferta, Integer stock, Integer cantidad) {}

    private String ownerKey(HttpSession session) {
        Usuario user = auth.currentUser(session);
        return user == null ? "SESSION:" + session.getId() : "USER:" + user.getId();
    }
    private BigDecimal price(Producto p) {
        return p.isEnOferta() && p.getPrecioOferta() != null && p.getPrecioOferta().compareTo(BigDecimal.ZERO) > 0 && p.getPrecioOferta().compareTo(p.getPrecio()) < 0
                ? p.getPrecioOferta() : p.getPrecio();
    }
    @GetMapping @Transactional(readOnly = true)
    public List<CartLine> get(HttpSession session) {
        return carrito.findByOwnerKey(ownerKey(session)).stream().map(item -> {
            Producto p = item.getProducto();
            return new CartLine(item.getId(), p.getId(), p.getNombre(), price(p), p.getImagen(), p.getCategoria(), p.isEnOferta(), p.getPrecioOferta(), p.getStock(), item.getCantidad());
        }).toList();
    }
    @PostMapping @Transactional
    public ResponseEntity<?> add(@RequestBody CartInput input, HttpSession session) {
        if (input.productoId() == null) return ResponseEntity.badRequest().body(Map.of("message", "Selecciona un producto válido."));
        Producto p = productos.findById(input.productoId()).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        int add = input.cantidad() == null || input.cantidad() < 1 ? 1 : input.cantidad();
        String owner = ownerKey(session);
        CarritoItem item = carrito.findByOwnerKeyAndProductoId(owner, p.getId()).orElseGet(() -> {
            CarritoItem fresh = new CarritoItem(); fresh.setOwnerKey(owner); fresh.setProducto(p); fresh.setCantidad(0); return fresh;
        });
        int next = item.getCantidad() + add;
        if (p.getStock() != null && next > p.getStock())
            return ResponseEntity.status(409).body(Map.of("message", "No hay stock suficiente para " + p.getNombre() + ". Stock disponible: " + p.getStock()));
        item.setCantidad(next); carrito.save(item);
        return ResponseEntity.ok(Map.of("message", "Producto agregado al carrito."));
    }
    @PutMapping("/items/{productoId}") @Transactional
    public ResponseEntity<?> update(@PathVariable Long productoId, @RequestBody CartInput input, HttpSession session) {
        CarritoItem item = carrito.findByOwnerKeyAndProductoId(ownerKey(session), productoId).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        int quantity = input.cantidad() == null ? 1 : input.cantidad();
        if (quantity < 1) return ResponseEntity.badRequest().body(Map.of("message", "La cantidad debe ser mayor que cero."));
        if (quantity > item.getProducto().getStock()) return ResponseEntity.status(409).body(Map.of("message", "No hay stock suficiente."));
        item.setCantidad(quantity); carrito.save(item);
        return ResponseEntity.ok(Map.of("message", "Carrito actualizado."));
    }
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<?> remove(@PathVariable Long productoId, HttpSession session) {
        carrito.findByOwnerKeyAndProductoId(ownerKey(session), productoId).ifPresent(carrito::delete);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping @Transactional
    public ResponseEntity<?> clear(HttpSession session) {
        carrito.deleteByOwnerKey(ownerKey(session));
        return ResponseEntity.noContent().build();
    }
}
