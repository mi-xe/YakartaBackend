package com.example.demo.controller;

import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:4173"})
@Tag(name = "Categorías", description = "Categorías disponibles para clasificar juegos")
public class CategoriaController {
    private final CategoriaRepository repo;
    public CategoriaController(CategoriaRepository repo) { this.repo = repo; }

    @GetMapping
    @Operation(summary = "Listar categorías")
    public List<Categoria> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<Categoria> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear categoría")
    public ResponseEntity<?> create(@RequestBody Categoria c) {
        c.setId(null);
        if (c.getNombre() == null || c.getNombre().isBlank())
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "El nombre es obligatorio"));
        if (repo.findAll().stream().anyMatch(x -> x.getNombre().equalsIgnoreCase(c.getNombre().trim())))
            return ResponseEntity.status(409).body(java.util.Map.of("message", "La categoría ya existe"));
        c.setNombre(c.getNombre().trim());
        return ResponseEntity.status(201).body(repo.save(c));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    public ResponseEntity<Categoria> update(@PathVariable Long id, @RequestBody Categoria c) {
        return repo.findById(id).map(old -> {
            old.setNombre(c.getNombre().trim());
            return ResponseEntity.ok(repo.save(old));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
