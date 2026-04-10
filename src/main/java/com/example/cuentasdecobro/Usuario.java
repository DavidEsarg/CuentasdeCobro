package com.example.cuentasdecobro;

import java.util.List;

public class Usuario {
    private String nombreUsuario;
    private String password;
    private String nombres;
    private String apellidos;
    private String cedula;
    private List<String> entidades;
    private List<String> contratos;

    public Usuario(String nombreUsuario, String password, String nombres,
                   String apellidos, String cedula,
                   List<String> entidades, List<String> contratos) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cedula = cedula;
        this.entidades = entidades;
        this.contratos = contratos;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getPassword() { return password; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCedula() { return cedula; }
    public List<String> getEntidades() { return entidades; }
    public List<String> getContratos() { return contratos; }
}