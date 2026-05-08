package com.example.cuentasdecobro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SecopService {

    private static final String API_URL =
            "https://www.datos.gov.co/resource/p6dx-8zbt.json?numero_contrato=";

    public static List<String> buscarObligaciones(String numeroContrato) {
        List<String> obligaciones = new ArrayList<>();
        try {
            String contratoEncoded = URLEncoder.encode(numeroContrato, StandardCharsets.UTF_8);
            URL url = new URL(API_URL + contratoEncoded);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);

            int status = con.getResponseCode();
            if (status != 200) {
                obligaciones.add("SECOP no respondió correctamente (código " + status + ")");
                return obligaciones;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String json = sb.toString();

            if (json.equals("[]") || json.isEmpty()) {
                obligaciones.add("No se encontró el contrato: " + numeroContrato);
                return obligaciones;
            }

            if (json.contains("\"objeto_del_contrato\"")) {
                String v = json.split("\"objeto_del_contrato\":\"")[1].split("\"")[0];
                obligaciones.add("Objeto: " + v);
            }
            if (json.contains("\"descripcion_del_proceso\"")) {
                String v = json.split("\"descripcion_del_proceso\":\"")[1].split("\"")[0];
                obligaciones.add("Descripcion: " + v);
            }
            if (json.contains("\"nombre_entidad\"")) {
                String v = json.split("\"nombre_entidad\":\"")[1].split("\"")[0];
                obligaciones.add("Entidad: " + v);
            }
            if (json.contains("\"valor_del_contrato\"")) {
                String v = json.split("\"valor_del_contrato\":\"")[1].split("\"")[0];
                obligaciones.add("Valor: $" + v);
            }
            if (json.contains("\"fecha_de_inicio_del_contrato\"")) {
                String v = json.split("\"fecha_de_inicio_del_contrato\":\"")[1].split("\"")[0];
                obligaciones.add("Inicio: " + v);
            }
            if (json.contains("\"fecha_de_fin_del_contrato\"")) {
                String v = json.split("\"fecha_de_fin_del_contrato\":\"")[1].split("\"")[0];
                obligaciones.add("Fin: " + v);
            }
            if (obligaciones.isEmpty()) {
                obligaciones.add("Contrato encontrado pero sin detalle disponible");
            }

        } catch (Exception e) {
            obligaciones.add("Error al consultar SECOP: " + e.getMessage());
        }
        return obligaciones;
    }
}