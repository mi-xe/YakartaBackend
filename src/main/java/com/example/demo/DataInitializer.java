package com.example.demo;

import com.example.demo.model.Categoria;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {
    private record ProductSeed(String nombre, int precio, String imagen, String categoria, boolean oferta, Integer precioOferta) {}

    @Bean
    CommandLineRunner initializeCatalog(ProductoRepository products, CategoriaRepository categories, UsuarioRepository users) {
        return args -> {
            if (categories.count() == 0) {
                for (String name : List.of("Acción", "Aventura", "RPG", "Estrategia", "Deportes", "Carreras", "Terror", "Lucha", "Simulación")) {
                    Categoria category = new Categoria(); category.setNombre(name); categories.save(category);
                }
            }
            if (products.count() == 0) {
                List<ProductSeed> seeds = List.of(
                    new ProductSeed("Midnight club",15000,"imgtienda/Midnight_Club-Los_Angeles.jpg","Carreras",true,12000),
                    new ProductSeed("Fifa street 2",10000,"imgtienda/fifastreet.jpg","Deportes",false,null),
                    new ProductSeed("Black",5000,"imgtienda/black.jpg","Acción",true,4000),
                    new ProductSeed("Burnout 3: takedown",18000,"imgtienda/burnout 3.jpg","Carreras",false,null),
                    new ProductSeed("Dragon ball tenkaichi 3",13000,"imgtienda/dragonball.webp","Lucha",false,null),
                    new ProductSeed("Peak",5500,"imgtienda/peak.jpg","Aventura",false,null),
                    new ProductSeed("Zelda: Ocarina of time",60000,"imgtienda/zelda.jpg","Aventura",false,null),
                    new ProductSeed("The outlast trials",34000,"imgtienda/the oullasttrials.jpg","Terror",true,29000),
                    new ProductSeed("Undertale",8000,"imgtienda/undertae.webp","RPG",false,null),
                    new ProductSeed("NFS: shift",25000,"imgtienda/nfs shift.jpg","Carreras",false,null),
                    new ProductSeed("Bordelands 2",6500,"imgtienda/bordelands 2.jpg","Acción",false,null),
                    new ProductSeed("Warcraft 3: Frozen throne",15000,"imgtienda/warcraft 3.webp","Estrategia",false,null),
                    new ProductSeed("Gran turismo 5",20000,"imgtienda/gran turismo 5.jpg","Carreras",false,null),
                    new ProductSeed("Dead by daylight",10000,"imgtienda/dbd.jpg","Terror",true,7500),
                    new ProductSeed("Transformers: the game",15500,"imgtienda/transformersgame.jpg","Acción",false,null),
                    new ProductSeed("Gran turismo 4",17000,"imgtienda/granturismo4.jpg","Carreras",false,null),
                    new ProductSeed("Detroit",15000,"imgtienda/detroit.jpg","Aventura",false,null),
                    new ProductSeed("Red dead redemption 2",20000,"imgtienda/rdr2.webp","Aventura",false,null),
                    new ProductSeed("Papers please",8000,"imgtienda/papersplease.webp","Estrategia",false,null),
                    new ProductSeed("Mortal kombat 1",67000,"imgtienda/incompleto1.jpg","Lucha",true,59000),
                    new ProductSeed("Resident evil requiem",40000,"imgtienda/rd9.jpg","Terror",false,null),
                    new ProductSeed("Pink extinct reprieve 2",22000,"imgtienda/pink extinct reprieve 2.jpg","Aventura",false,null),
                    new ProductSeed("Webfishing",3000,"imgtienda/webfishing.jpg","Simulación",false,null),
                    new ProductSeed("Automobilista 2",9000,"imgtienda/at2.jpg","Simulación",false,null),
                    new ProductSeed("Left 4 dead 2",5500,"imgtienda/left 4 dead 2.jpg","Acción",true,4500),
                    new ProductSeed("Cyberpunk",25500,"imgtienda/cyberpunk.webp","RPG",false,null),
                    new ProductSeed("GTA VI",100000,"imgtienda/gta6.webp","Aventura",false,null),
                    new ProductSeed("Blue alive condemnation 2",21000,"imgtienda/blue alive condemnation 2.png","Acción",false,null)
                );
                for (ProductSeed seed : seeds) {
                    Producto product = new Producto();
                    product.setNombre(seed.nombre()); product.setPrecio(BigDecimal.valueOf(seed.precio()));
                    product.setImagen(seed.imagen()); product.setCategoria(seed.categoria());
                    product.setEnOferta(seed.oferta());
                    product.setPrecioOferta(seed.precioOferta() == null ? null : BigDecimal.valueOf(seed.precioOferta()));
                    product.setStock(10); products.save(product);
                }
            }
            if (users.findByEmail("admin@tienda.com").isEmpty()) {
                Usuario admin = new Usuario(); admin.setNombre("Administrador Yakarta");
                admin.setEmail("admin@tienda.com"); admin.setPassword(PasswordUtil.hash("123"));
                admin.setRol("Administrador"); admin.setSaldo(BigDecimal.ZERO); users.save(admin);
            }
        };
    }
}
