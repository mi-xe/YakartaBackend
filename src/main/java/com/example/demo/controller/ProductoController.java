package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.service.SessionAuth;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoRepository repo;
    private final SessionAuth auth;
    private final CarritoItemRepository carrito;

    public ProductoController(ProductoRepository repo, SessionAuth auth, CarritoItemRepository carrito) {
        this.repo = repo;
        this.auth = auth;
        this.carrito = carrito;
    }

    /** Public catalog endpoint: returns the same persisted records used by the admin panel. */
    @GetMapping
    public List<Producto> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Producto product, HttpSession session) {
        if (!auth.isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("message", "Se requiere rol Administrador."));
        }
        String validation = validate(product);
        if (validation != null) return ResponseEntity.badRequest().body(Map.of("message", validation));

        product.setId(null);
        normalize(product);
        return ResponseEntity.status(201).body(repo.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Producto product, HttpSession session) {
        if (!auth.isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("message", "Se requiere rol Administrador."));
        }
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        String validation = validate(product);
        if (validation != null) return ResponseEntity.badRequest().body(Map.of("message", validation));

        product.setId(id);
        normalize(product);
        return ResponseEntity.ok(repo.save(product));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (!auth.isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("message", "Se requiere rol Administrador."));
        }
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        // Sin FK física para mantener compatibilidad con el ID INT UNSIGNED heredado.
        carrito.deleteByProducto_Id(id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String validate(Producto product) {
        if (product == null || product.getNombre() == null || product.getNombre().isBlank()) {
            return "El nombre del producto es obligatorio.";
        }
        if (product.getPrecio() == null || product.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            return "El precio debe ser un número mayor o igual a cero.";
        }
        if (product.getStock() == null || product.getStock() < 0) {
            return "El stock debe ser un número mayor o igual a cero.";
        }
        if (product.getCategoria() == null || product.getCategoria().isBlank()) {
            return "Selecciona una categoría para el producto.";
        }
        if (product.getImagen() != null && product.getImagen().length() > 255) {
            return "La ruta o URL de imagen no puede superar 255 caracteres.";
        }
        return null;
    }

    private void normalize(Producto product) {
        product.setNombre(product.getNombre().trim());
        product.setCategoria(product.getCategoria().trim());
        product.setImagen(product.getImagen() == null || product.getImagen().isBlank()
                ? "imgtienda/peak.jpg" : product.getImagen().trim());
        if (product.getPrecioOferta() == null || product.getPrecioOferta().compareTo(BigDecimal.ZERO) <= 0) {
            product.setPrecioOferta(null);
        }
        if (product.getPrecioOferta() == null
                || product.getPrecioOferta().compareTo(product.getPrecio()) >= 0) {
            product.setEnOferta(false);
        }
    }
}
