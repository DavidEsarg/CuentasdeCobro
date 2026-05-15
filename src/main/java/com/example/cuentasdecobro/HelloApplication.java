package com.example.cuentasdecobro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
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
        new Thread(() -> {
            ConexionDB.cerrarMongo();
            AppLogger.info("SISTEMA", "CIERRE_APP", "Aplicación cerrada.");
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}