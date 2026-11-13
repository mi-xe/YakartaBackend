package com.example.demo.controller;
import com.example.demo.model.Producto; import com.example.demo.repository.ProductoRepository;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.*;




@RestController @RequestMapping("/api/productos")
public class ProductoController {
 private final ProductoRepository repo; public ProductoController(ProductoRepository repo){this.repo=repo;}
 @GetMapping public List<Producto> all(){return repo.findAll();}
 @GetMapping("/{id}") public ResponseEntity<Producto> one(@PathVariable Long id){return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());}
 @PostMapping public Producto create(@RequestBody Producto p){p.setId(null);return repo.save(p);}
 @PutMapping("/{id}") public ResponseEntity<Producto> update(@PathVariable Long id,@RequestBody Producto p){return repo.findById(id).map(old->{p.setId(id);return ResponseEntity.ok(repo.save(p));}).orElse(ResponseEntity.notFound().build());}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){if(!repo.existsById(id))return ResponseEntity.notFound().build();repo.deleteById(id);return ResponseEntity.noContent().build();}
}
