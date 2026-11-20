package com.example.demo.controller;

import com.example.demo.model.CarritoItem;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.PasswordUtil;
import com.example.demo.service.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioRepository usuarios;
    private final CarritoItemRepository carrito;
    private final SessionAuth auth;

    public AuthController(UsuarioRepository usuarios, CarritoItemRepository carrito, SessionAuth auth) {
        this.usuarios = usuarios; this.carrito = carrito; this.auth = auth;
    }

    public record Credentials(String nombre, String email, String password) {}
    public record FundsRequest(BigDecimal amount) {}
    public record PublicUser(Long id, String nombre, String email, String rol, BigDecimal saldo) {}

    private PublicUser safe(Usuario user) {
        return new PublicUser(user.getId(), user.getNombre(), user.getEmail(), user.getRol(), user.getSaldo());
    }

    @PostMapping("/registro")
    public ResponseEntity<?> register(@RequestBody Credentials input) {
        if (input.nombre() == null || input.nombre().isBlank() || input.email() == null ||
                !input.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                input.password() == null || input.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Completa los datos y usa una contraseña de al menos 6 caracteres."));
        }
        String email = input.email().trim().toLowerCase();
        if (usuarios.findByEmail(email).isPresent())
            return ResponseEntity.status(409).body(Map.of("message", "Ese correo ya está registrado."));
        Usuario user = new Usuario();
        user.setNombre(input.nombre().trim()); user.setEmail(email);
        user.setPassword(PasswordUtil.hash(input.password())); user.setRol("Cliente"); user.setSaldo(BigDecimal.ZERO);
        usuarios.save(user);
        return ResponseEntity.status(201).body(Map.of("user", safe(user), "message", "Cuenta creada correctamente."));
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody Credentials input, HttpServletRequest request, HttpSession session) {
        if (input.email() == null || input.password() == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Ingresa tu correo y contraseña."));
        var found = usuarios.findByEmail(input.email().trim().toLowerCase());
        if (found.isEmpty() || !PasswordUtil.verify(input.password(), found.get().getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Correo o contraseña incorrectos."));

        String oldOwner = "SESSION:" + session.getId();
        request.changeSessionId();
        Usuario user = found.get();
        session.setAttribute(SessionAuth.USER_ID, user.getId());
        String userOwner = "USER:" + user.getId();
        for (CarritoItem item : carrito.findByOwnerKey(oldOwner)) {
            var existing = carrito.findByOwnerKeyAndProductoId(userOwner, item.getProducto().getId());
            if (existing.isPresent()) {
                CarritoItem current = existing.get();
                current.setCantidad(current.getCantidad() + item.getCantidad());
                carrito.save(current);
                carrito.delete(item);
            } else {
                item.setOwnerKey(userOwner);
                carrito.save(item);
            }
        }
        return ResponseEntity.ok(Map.of("user", safe(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Usuario user = auth.currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "No hay una sesión iniciada."));
        return ResponseEntity.ok(Map.of("user", safe(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada."));
    }

    @PostMapping("/fondos")
    @Transactional
    public ResponseEntity<?> addFunds(@RequestBody FundsRequest input, HttpSession session) {
        Usuario user = auth.currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Inicia sesión para agregar fondos."));
        if (!"Cliente".equalsIgnoreCase(user.getRol()))
            return ResponseEntity.status(403).body(Map.of("message", "Solo las cuentas Cliente pueden agregar fondos."));
        if (input.amount() == null || input.amount().compareTo(BigDecimal.ZERO) <= 0 || input.amount().compareTo(new BigDecimal("1000000")) > 0)
            return ResponseEntity.badRequest().body(Map.of("message", "El monto solicitado no es válido."));
        user.setSaldo(user.getSaldo().add(input.amount()));
        usuarios.save(user);
        return ResponseEntity.ok(Map.of("user", safe(user), "message", "Saldo actualizado."));
    }
}
