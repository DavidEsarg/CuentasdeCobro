package com.example.cuentasdecobro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Inicializar colecciones MongoDB (índices) al arrancar la app
        MongoService.inicializarColecciones();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/cuentasdecobro/login.fxml")
        );
        Scene scene = new Scene(loader.load());
        stage.setTitle("Login - Sistema de cuentas de cobro");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Cerrar conexión MongoDB al cerrar la app
        ConexionDB.cerrarMongo();
    }

    public static void main(String[] args) {
        launch();
    }
}