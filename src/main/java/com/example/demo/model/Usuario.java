package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, length = 180, unique = true)
    private String email;
    // Guarda aquí un hash seguro, nunca la contraseña en texto plano.
    @Column(nullable = false, length = 255)
    private String password;
    @Column(nullable = false, length = 30)
    private String rol = "Cliente";

    public Usuario() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
    public String getRol(){return rol;} public void setRol(String rol){this.rol=rol;}
}
