package com.example.cuentasdecobro;

public class ValidadorColombia {

    public static boolean esCedulaValida(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        String soloNumeros = cedula.replaceAll("[^0-9]", "");
        return soloNumeros.length() >= 6 && soloNumeros.length() <= 10;
    }

    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null || telefono.isBlank()) return true;
        String soloNumeros = telefono.replaceAll("[^0-9]", "");
        return soloNumeros.length() == 10 && soloNumeros.startsWith("3");
    }

    public static boolean esEmailValido(String email) {
        if (email == null || email.isBlank()) return true;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static String formatearCedula(String cedula) {
        return cedula.replaceAll("[^0-9]", "");
    }

    public static String generarNumeroCuenta(int consecutivo) {
        return String.format("CC-%d-%04d",
                java.time.LocalDate.now().getYear(), consecutivo);
    }

    public static int obtenerSiguienteConsecutivo() {
        try (java.sql.Connection con = ConexionDB.getConexionSQLite();
             java.sql.Statement st = con.createStatement()) {
            st.execute("UPDATE contador_cuentas SET ultimo_numero = ultimo_numero + 1 WHERE id = 1");
            java.sql.ResultSet rs = st.executeQuery(
                    "SELECT ultimo_numero FROM contador_cuentas WHERE id = 1");
            if (rs.next()) return rs.getInt("ultimo_numero");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}