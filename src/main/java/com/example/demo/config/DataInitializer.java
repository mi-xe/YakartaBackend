package com.example.demo.config;

import com.example.demo.model.Categoria;
import com.example.demo.model.Producto;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner loadInitialData(CategoriaRepository categorias, ProductoRepository productos) {
        return args -> {
            List<String> nombresCategorias = List.of("Acción", "Aventura", "RPG", "Estrategia",
                "Deportes", "Carreras", "Terror", "Lucha", "Simulación");
            for (String nombre : nombresCategorias) {
                if (categorias.findAll().stream().noneMatch(c -> c.getNombre().equalsIgnoreCase(nombre))) {
                    Categoria c = new Categoria();
                    c.setNombre(nombre);
                    categorias.save(c);
                }
            }
            if (productos.count() == 0) {
                crear(productos, "Midnight club", 15000L, "imgtienda/Midnight_Club-Los_Angeles.jpg", "Carreras", true, 12000L, 12, "PlayStation 3");
                crear(productos, "Fifa street 2", 10000L, "imgtienda/fifastreet.jpg", "Deportes", false, null, 5, "PSP");
                crear(productos, "Black", 5000L, "imgtienda/black.jpg", "Acción", true, 4000L, 9, "PlayStation 2");
                crear(productos, "Burnout 3: takedown", 18000L, "imgtienda/burnout 3.jpg", "Carreras", false, null, 14, "PlayStation 2");
                crear(productos, "Dragon ball tenkaichi 3", 13000L, "imgtienda/dragonball.webp", "Lucha", false, null, 3, "PlayStation 2");
                crear(productos, "Peak", 5500L, "imgtienda/peak.jpg", "Aventura", false, null, 8, "PC", "PlayStation 5", "Xbox Series S/X");
                crear(productos, "Zelda: Ocarina of time", 60000L, "imgtienda/zelda.jpg", "Aventura", false, null, 11, "Nintendo Switch");
                crear(productos, "The outlast trials", 34000L, "imgtienda/the oullasttrials.jpg", "Terror", true, 29000L, 6, "PC", "PlayStation 4", "Xbox Series S/X");
                crear(productos, "Undertale", 8000L, "imgtienda/undertae.webp", "RPG", false, null, 15, "PC", "PlayStation 5", "Xbox Series S/X", "Nintendo Switch");
                crear(productos, "NFS: shift", 25000L, "imgtienda/nfs shift.jpg", "Carreras", false, null, 4, "PC", "PlayStation 3");
                crear(productos, "Bordelands 2", 6500L, "imgtienda/bordelands 2.jpg", "Acción", false, null, 13, "PC");
                crear(productos, "Warcraft 3: Frozen throne", 15000L, "imgtienda/warcraft 3.webp", "Estrategia", false, null, 7, "PC");
                crear(productos, "Gran turismo 5", 20000L, "imgtienda/gran turismo 5.jpg", "Carreras", false, null, 2, "PlayStation 3");
                crear(productos, "Dead by daylight", 10000L, "imgtienda/dbd.jpg", "Terror", true, 7500L, 10, "PC");
                crear(productos, "Transformers: the game", 15500L, "imgtienda/transformersgame.jpg", "Acción", false, null, 5, "PlayStation 2", "PlayStation 3");
                crear(productos, "Gran turismo 4", 17000L, "imgtienda/granturismo4.jpg", "Carreras", false, null, 14, "PlayStation 2");
                crear(productos, "Detroit", 15000L, "imgtienda/detroit.jpg", "Aventura", false, null, 8, "PC", "PlayStation 4");
                crear(productos, "Red dead redemption 2", 20000L, "imgtienda/rdr2.webp", "Aventura", false, null, 1, "PC", "PlayStation 4", "Xbox Series S/X");
                crear(productos, "Papers please", 8000L, "imgtienda/papersplease.webp", "Estrategia", false, null, 12, "PC");
                crear(productos, "Mortal kombat 1", 67000L, "imgtienda/incompleto1.jpg", "Lucha", true, 59000L, 6, "PC", "PlayStation 5", "Xbox Series S/X", "Nintendo Switch");
                crear(productos, "Resident evil requiem", 40000L, "imgtienda/rd9.jpg", "Terror", false, null, 9, "PC", "PlayStation 5", "Xbox Series S/X");
                crear(productos, "Pink extinct reprieve 2", 22000L, "imgtienda/pink extinct reprieve 2.jpg", "Aventura", false, null, 3, "PC", "PlayStation 5", "PlayStation 4", "PlayStation 3", "PSP", "Xbox Series S/X", "Nintendo Switch");
                crear(productos, "Webfishing", 3000L, "imgtienda/webfishing.jpg", "Simulación", false, null, 11, "PC");
                crear(productos, "Automobilista 2", 9000L, "imgtienda/at2.jpg", "Simulación", false, null, 15, "PC");
                crear(productos, "Left 4 dead 2", 5500L, "imgtienda/left 4 dead 2.jpg", "Acción", true, 4500L, 4, "PC");
                crear(productos, "Cyberpunk", 25500L, "imgtienda/cyberpunk.webp", "RPG", false, null, 7, "PC", "Nintendo Switch 2");
                crear(productos, "GTA VI", 100000L, "imgtienda/gta6.webp", "Aventura", false, null, 0, "PlayStation 5", "Xbox Series S/X");
                crear(productos, "Blue alive condemnation 2", 21000L, "imgtienda/blue alive condemnation 2.png", "Acción", false, null, 10, "PC", "PlayStation 5", "PlayStation 4", "PlayStation 3", "PlayStation 2", "PSP", "Xbox Series S/X", "Nintendo Switch", "Nintendo Switch 2");
            }
        };
    }

    private void crear(ProductoRepository repo, String nombre, long precio, String imagen,
                       String categoria, boolean enOferta, Long precioOferta,
                       int stock, String... plataformas) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(BigDecimal.valueOf(precio));
        p.setImagen(imagen);
        p.setCategoria(categoria);
        p.setEnOferta(enOferta);
        p.setPrecioOferta(precioOferta == null ? null : BigDecimal.valueOf(precioOferta));
        p.setStock(stock);
        p.setPlataformas(List.of(plataformas));
        repo.save(p);
    }
}
