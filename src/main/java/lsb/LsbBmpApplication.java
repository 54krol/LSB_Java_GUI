package lsb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lsb.controller.MainController;
import lsb.view.MainView;

/** Главное JavaFX-приложение. */
public class LsbBmpApplication extends Application {

    private static final Logger LOGGER = LogManager.getLogger(LsbBmpApplication.class);

    /** Создает окно приложения. */
    @Override
    public void start(Stage stage) {
        MainView view = new MainView();
        new MainController(stage, view);

        Scene scene = new Scene(view.getRoot(), 1100, 720);
        stage.setTitle("LSB-стеганография BMP");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();

        LOGGER.info("Приложение запущено");
    }
}
