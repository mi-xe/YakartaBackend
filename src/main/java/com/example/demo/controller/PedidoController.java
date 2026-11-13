package com.example.demo.controller;
import com.example.demo.model.*; import com.example.demo.repository.*; import org.springframework.http.*; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;



@RestController @RequestMapping("/api/pedidos") public class PedidoController {
 private final PedidoRepository pedidos; private final UsuarioRepository usuarios; private final ProductoRepository productos;
 public PedidoController(PedidoRepository p,UsuarioRepository u,ProductoRepository pr){pedidos=p;usuarios=u;productos=pr;}
 record Linea(Long productoId,String nombre,Integer cantidad,BigDecimal precio){} record PedidoIn(Long usuarioId,BigDecimal total,String estado,List<Linea> productos){}
 record LineaOut(Long productoId,String nombre,Integer cantidad,BigDecimal precio){} record PedidoOut(Long id,Long usuarioId,String usuarioEmail,LocalDate fecha,BigDecimal total,String estado,List<LineaOut> productos){}
 private PedidoOut out(Pedido p){return new PedidoOut(p.getId(),p.getUsuario()==null?null:p.getUsuario().getId(),p.getUsuario()==null?null:p.getUsuario().getEmail(),p.getFecha(),p.getTotal(),p.getEstado(),p.getProductos().stream().map(d->new LineaOut(d.getProducto()==null?null:d.getProducto().getId(),d.getNombreProducto(),d.getCantidad(),d.getPrecioUnitario())).toList());}
 @GetMapping @Transactional(readOnly=true) public List<PedidoOut> all(){return pedidos.findAll().stream().map(this::out).toList();}
 @GetMapping("/{id}") @Transactional(readOnly=true) public ResponseEntity<PedidoOut> one(@PathVariable Long id){return pedidos.findById(id).map(p->ResponseEntity.ok(out(p))).orElse(ResponseEntity.notFound().build());}
 @PostMapping @Transactional public ResponseEntity<?> create(@RequestBody PedidoIn in){Usuario u=usuarios.findById(in.usuarioId()).orElse(null);if(u==null)return ResponseEntity.badRequest().body(Map.of("message","Usuario no encontrado"));Pedido p=new Pedido();p.setUsuario(u);p.setFecha(LocalDate.now());p.setTotal(in.total()==null?BigDecimal.ZERO:in.total());p.setEstado(in.estado()==null?"Entregado":in.estado());for(Linea l:in.productos()){PedidoDetalle d=new PedidoDetalle();d.setPedido(p);d.setProducto(l.productoId()==null?null:productos.findById(l.productoId()).orElse(null));d.setNombreProducto(l.nombre());d.setCantidad(l.cantidad());d.setPrecioUnitario(l.precio());p.getProductos().add(d);}return ResponseEntity.status(201).body(out(pedidos.save(p)));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){if(!pedidos.existsById(id))return ResponseEntity.notFound().build();pedidos.deleteById(id);return ResponseEntity.noContent().build();}
}
