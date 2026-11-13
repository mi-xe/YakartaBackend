package com.example.demo.controller;
import com.example.demo.model.Categoria; import com.example.demo.repository.CategoriaRepository; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.*;




@RestController @RequestMapping("/api/categorias") public class CategoriaController {
 private final CategoriaRepository repo; public CategoriaController(CategoriaRepository repo){this.repo=repo;}
 @GetMapping public List<Categoria> all(){return repo.findAll();}
 @PostMapping public Categoria create(@RequestBody Categoria c){c.setId(null);return repo.save(c);}
 @PutMapping("/{id}") public ResponseEntity<Categoria> update(@PathVariable Long id,@RequestBody Categoria c){if(!repo.existsById(id))return ResponseEntity.notFound().build();c.setId(id);return ResponseEntity.ok(repo.save(c));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){if(!repo.existsById(id))return ResponseEntity.notFound().build();repo.deleteById(id);return ResponseEntity.noContent().build();}
}
