package com.example.cuentasdecobro;

import java.time.LocalDate;

public class Cuenta {
    private String numero;
    private String funcionario;
    private String entidad;
    private LocalDate fecha;
    private String valor;
    private String estado;

    public Cuenta(String numero, String funcionario, String entidad,
                  LocalDate fecha, String valor, String estado) {
        this.numero = numero;
        this.funcionario = funcionario;
        this.entidad = entidad;
        this.fecha = fecha;
        this.valor = valor;
        this.estado = estado;
    }

    public String getNumero() { return numero; }
    public String getFuncionario() { return funcionario; }
    public String getEntidad() { return entidad; }
    public LocalDate getFecha() { return fecha; }
    public String getValor() { return valor; }
    public String getEstado() { return estado; }
}