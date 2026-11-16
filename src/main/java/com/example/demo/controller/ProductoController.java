package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:4173"})
@Tag(name = "Juegos / Productos", description = "Administración del catálogo de juegos de Yakarta")
public class ProductoController {
    private final ProductoRepository repo;
    public ProductoController(ProductoRepository repo) { this.repo = repo; }

    @GetMapping
    @Operation(summary = "Listar juegos", description = "Devuelve todos los juegos disponibles en el catálogo.")
    public List<Producto> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener juego por ID")
    public ResponseEntity<Producto> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear juego", description = "Registra un juego nuevo. No envíes el campo id.")
    public ResponseEntity<Producto> create(@RequestBody Producto p) {
        p.setId(null);
        if (p.getNombre() == null || p.getNombre().isBlank() || p.getPrecio() == null)
            return ResponseEntity.badRequest().build();
        if (p.getStock() == null) p.setStock(10);
        if (p.getCategoria() == null || p.getCategoria().isBlank()) p.setCategoria("Otros");
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar juego")
    public ResponseEntity<Producto> update(@PathVariable Long id, @RequestBody Producto p) {
        return repo.findById(id).map(old -> {
            p.setId(id);
            if (p.getStock() == null) p.setStock(old.getStock());
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar juego")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
