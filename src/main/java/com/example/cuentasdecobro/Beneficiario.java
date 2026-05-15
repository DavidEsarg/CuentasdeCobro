package com.example.cuentasdecobro;

public class Beneficiario {
    private int id;
    private String nombre;
    private String cedula;
    private String banco;
    private String tipoCuenta;
    private String numeroCuenta;
    private String email;
    private String telefono;

    public Beneficiario(int id, String nombre, String cedula, String banco,
                        String tipoCuenta, String numeroCuenta,
                        String email, String telefono) {
        this.id = id; this.nombre = nombre; this.cedula = cedula;
        this.banco = banco; this.tipoCuenta = tipoCuenta;
        this.numeroCuenta = numeroCuenta;
        this.email = email; this.telefono = telefono;
    }

    public int getId()            { return id; }
    public String getNombre()     { return nombre; }
    public String getCedula()     { return cedula; }
    public String getBanco()      { return banco; }
    public String getTipoCuenta() { return tipoCuenta; }
    public String getNumeroCuenta(){ return numeroCuenta; }
    public String getEmail()      { return email; }
    public String getTelefono()   { return telefono; }
}