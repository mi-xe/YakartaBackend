package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.SessionAuth;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final CarritoItemRepository carrito;
    private final DireccionRepository direcciones;
    private final SessionAuth auth;

    public PedidoController(PedidoRepository pedidos, UsuarioRepository usuarios, ProductoRepository productos,
                            CarritoItemRepository carrito, DireccionRepository direcciones, SessionAuth auth) {
        this.pedidos = pedidos; this.usuarios = usuarios; this.productos = productos;
        this.carrito = carrito; this.direcciones = direcciones; this.auth = auth;
    }

    public record LineaOut(Long productoId, String nombre, Integer cantidad, BigDecimal precio) {}
    public record DireccionOut(String nombre, String calle, String comuna, String telefono) {}
    public record PedidoOut(Long id, Long usuarioId, String usuarioEmail, LocalDate fecha, BigDecimal total,
                            String estado, List<LineaOut> productos, DireccionOut direccion) {}

    private BigDecimal effectivePrice(Producto product) {
        return product.isEnOferta() && product.getPrecioOferta() != null
                && product.getPrecioOferta().compareTo(BigDecimal.ZERO) > 0
                && product.getPrecioOferta().compareTo(product.getPrecio()) < 0
                ? product.getPrecioOferta() : product.getPrecio();
    }

    private PedidoOut out(Pedido order) {
        List<LineaOut> lines = order.getProductos().stream()
                .map(d -> new LineaOut(d.getProducto() == null ? null : d.getProducto().getId(),
                        d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario())).toList();
        DireccionOut address = new DireccionOut(order.getDireccionNombre(), order.getDireccionCalle(),
                order.getDireccionComuna(), order.getDireccionTelefono());
        Usuario owner = order.getUsuario();
        Long ownerId = owner == null ? null : owner.getId();
        String ownerEmail = owner == null ? order.getUsuarioEmail() : owner.getEmail();
        return new PedidoOut(order.getId(), ownerId, ownerEmail,
                order.getFecha(), order.getTotal(), order.getEstado(), lines, address);
    }

    @GetMapping @Transactional(readOnly = true)
    public ResponseEntity<?> all(HttpSession session) {
        Usuario user = auth.currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Inicia sesión para consultar tus compras."));
        List<Pedido> rows = "Administrador".equalsIgnoreCase(user.getRol())
                ? pedidos.findAll() : pedidos.findByUsuarioIdOrderByFechaDesc(user.getId());
        return ResponseEntity.ok(rows.stream().map(this::out).toList());
    }

    @GetMapping("/{id}") @Transactional(readOnly = true)
    public ResponseEntity<?> one(@PathVariable Long id, HttpSession session) {
        Usuario user = auth.currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Inicia sesión para consultar el pedido."));
        Pedido order = pedidos.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (!"Administrador".equalsIgnoreCase(user.getRol())
                && (order.getUsuario() == null || !order.getUsuario().getId().equals(user.getId())))
            return ResponseEntity.status(403).body(Map.of("message", "No tienes permiso para ver este pedido."));
        return ResponseEntity.ok(out(order));
    }

    // El servidor calcula el total desde los precios y cantidades persistidos en MySQL.
    @PostMapping @Transactional
    public ResponseEntity<?> create(HttpSession session) {
        Usuario user = auth.currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Inicia sesión para confirmar tu compra."));
        Direccion address = direcciones.findByUsuarioId(user.getId()).orElse(null);
        if (address == null) return ResponseEntity.badRequest().body(Map.of("message", "Antes de pagar debes guardar tus datos de despacho."));

        String owner = "USER:" + user.getId();
        List<CarritoItem> cart = carrito.findByOwnerKey(owner);
        if (cart.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Tu carrito está vacío."));

        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem item : cart) {
            Producto product = productos.findById(item.getProducto().getId()).orElse(null);
            if (product == null) return ResponseEntity.badRequest().body(Map.of("message", "Uno de los productos ya no está disponible."));
            if (item.getCantidad() == null || item.getCantidad() < 1 || product.getStock() < item.getCantidad())
                return ResponseEntity.status(409).body(Map.of("message", "Stock insuficiente para " + product.getNombre() + ". Stock disponible: " + product.getStock()));
            total = total.add(effectivePrice(product).multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        if ("Cliente".equalsIgnoreCase(user.getRol()) && user.getSaldo().compareTo(total) < 0)
            return ResponseEntity.status(402).body(Map.of("message", "Saldo insuficiente. Tu saldo actual es $ " + user.getSaldo().toPlainString() + " CLP."));

        Pedido order = new Pedido();
        order.setUsuario(user); order.setUsuarioNombre(user.getNombre()); order.setUsuarioEmail(user.getEmail());
        order.setFecha(LocalDate.now()); order.setEstado("Entregado"); order.setTotal(total);
        order.setDireccionNombre(address.getNombre()); order.setDireccionCalle(address.getCalle());
        order.setDireccionComuna(address.getComuna()); order.setDireccionTelefono(address.getTelefono());
        for (CarritoItem item : cart) {
            Producto product = productos.findById(item.getProducto().getId()).orElseThrow();
            BigDecimal unitPrice = effectivePrice(product);
            PedidoDetalle detail = new PedidoDetalle();
            detail.setPedido(order); detail.setProducto(product); detail.setNombreProducto(product.getNombre());
            detail.setCantidad(item.getCantidad()); detail.setPrecioUnitario(unitPrice);
            order.getProductos().add(detail);
            product.setStock(product.getStock() - item.getCantidad());
            productos.save(product);
        }
        if ("Cliente".equalsIgnoreCase(user.getRol())) {
            user.setSaldo(user.getSaldo().subtract(total));
            usuarios.save(user);
        }
        Pedido saved = pedidos.save(order);
        carrito.deleteByOwnerKey(owner);
        return ResponseEntity.status(201).body(Map.of("order", out(saved), "saldo", user.getSaldo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (!auth.isAdmin(session)) return ResponseEntity.status(403).body(Map.of("message", "Se requiere rol Administrador."));
        if (!pedidos.existsById(id)) return ResponseEntity.notFound().build();
        pedidos.deleteById(id); return ResponseEntity.noContent().build();
    }
}
