package com.example.cuentasdecobro;

public class SesionActual {

    private static Usuario usuarioActual;

    public static void set(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario get() {
        return usuarioActual;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}