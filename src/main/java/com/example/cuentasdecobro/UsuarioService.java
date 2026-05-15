package com.example.cuentasdecobro;

import java.util.ArrayList;
import java.util.Arrays;

public class UsuarioService {

    private static UsuarioService instancia;
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    private UsuarioService() {
        usuarios.add(new Usuario(
                "admin", "1234", "Administrador", "Sistema",
                "000000000",
                Arrays.asList("Entidad General"),
                Arrays.asList("CONTRATO-000"),
                "admin"
        ));
        usuarios.add(new Usuario(
                "revisor", "1234", "Revisor", "General",
                "000000001",
                Arrays.asList("Entidad General"),
                Arrays.asList("REVISION-000"),
                "revisor"
        ));
    }

    public static UsuarioService getInstancia() {
        if (instancia == null) instancia = new UsuarioService();
        return instancia;
    }

    public boolean registrar(Usuario usuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(usuario.getNombreUsuario())) return false;
        }
        usuarios.add(usuario);
        return true;
    }

    public Usuario autenticar(String nombreUsuario, String password) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(nombreUsuario) &&
                    u.getPassword().equals(password)) return u;
        }
        return null;
    }

    public ArrayList<Usuario> getUsuarios() { return usuarios; }
}