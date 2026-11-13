package com.example.demo.controller;
import com.example.demo.model.Usuario; import com.example.demo.repository.UsuarioRepository; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*; import java.security.*; import javax.crypto.SecretKeyFactory; import javax.crypto.spec.PBEKeySpec;




@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UsuarioRepository repo; public AuthController(UsuarioRepository repo){this.repo=repo;}
 record Credentials(String nombre,String email,String password){} record PublicUser(Long id,String nombre,String email,String rol){} record AuthResponse(PublicUser user){}
 private String hash(String password){try{byte[] salt=new byte[16];SecureRandom.getInstanceStrong().nextBytes(salt);byte[] key=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(),salt,120000,256)).getEncoded();return "pbkdf2$"+HexFormat.of().formatHex(salt)+"$"+HexFormat.of().formatHex(key);}catch(Exception e){throw new IllegalStateException(e);}}
 private boolean verify(String password,String stored){try{String[] parts=stored.split("\\$");if(parts.length!=3||!parts[0].equals("pbkdf2"))return false;byte[] salt=HexFormat.of().parseHex(parts[1]);byte[] expected=HexFormat.of().parseHex(parts[2]);byte[] actual=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(),salt,120000,256)).getEncoded();return MessageDigest.isEqual(expected,actual);}catch(Exception e){return false;}}
 private PublicUser safe(Usuario u){return new PublicUser(u.getId(),u.getNombre(),u.getEmail(),u.getRol());}
 @PostMapping("/registro") public ResponseEntity<?> register(@RequestBody Credentials c){if(c.nombre()==null||c.email()==null||c.password()==null||c.password().length()<6)return ResponseEntity.badRequest().body(Map.of("message","Completa los campos; la contraseña debe tener al menos 6 caracteres."));String email=c.email().trim().toLowerCase();if(repo.findByEmail(email).isPresent())return ResponseEntity.status(409).body(Map.of("message","Ese correo ya está registrado."));Usuario u=new Usuario();u.setNombre(c.nombre().trim());u.setEmail(email);u.setPassword(hash(c.password()));u.setRol("Cliente");repo.save(u);return ResponseEntity.status(201).body(Map.of("user",safe(u)));}
 @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Credentials c){
  Optional<Usuario> found=repo.findByEmail(c.email().trim().toLowerCase());
  if(found.isEmpty()||!verify(c.password(),found.get().getPassword()))return ResponseEntity.status(401).body(Map.of("message","Correo o contraseña incorrectos."));
  return ResponseEntity.ok(new AuthResponse(safe(found.get())));
 }
}
