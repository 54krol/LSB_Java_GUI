package lsb.controller;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lsb.EmbedResult;
import lsb.service.ImageService;
import lsb.service.LsbSteganographyService;
import lsb.view.MainView;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Обработчики кнопок главного окна. */
public class MainController {

    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    private final Stage stage;
    private final MainView view;
    private final ImageService imageService = new ImageService();
    private final LsbSteganographyService lsbService = new LsbSteganographyService();

    private BufferedImage originalImage;

    /** Создает контроллер и назначает события кнопкам. */
    public MainController(Stage stage, MainView view) {
        this.stage = stage;
        this.view = view;

        view.getChooseInputButton().setOnAction(e -> chooseInputImage());
        view.getChooseOutputButton().setOnAction(e -> chooseOutputImage());
        view.getEmbedButton().setOnAction(e -> embedAndSave());
        view.getMessageArea().textProperty().addListener((obs, oldText, newText) -> updateCapacity());
    }

    /** Выбирает и загружает исходное изображение. */
    private void chooseInputImage() {
        FileChooser chooser = createBmpChooser("Выберите исходный BMP");
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            originalImage = imageService.loadBmp(file.toPath());
            Path output = imageService.createDefaultOutputPath(file.toPath());

            view.setInputPath(file.getAbsolutePath());
            view.setOutputPath(output.toAbsolutePath().toString());
            view.showOriginalImage(imageService.toFxImage(originalImage));
            view.clearChangedBitsMask();
            view.setStatusText("Загружено: " + originalImage.getWidth() + " x " + originalImage.getHeight());
            updateCapacity();

            LOGGER.info("Открыт файл {}", file.getAbsolutePath());
        } catch (Exception e) {
            showError("Ошибка открытия", e);
        }
    }

    /** Выбирает путь для выходного BMP. */
    private void chooseOutputImage() {
        FileChooser chooser = createBmpChooser("Куда сохранить результат");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = imageService.ensureBmpExtension(file.toPath());
            view.setOutputPath(path.toAbsolutePath().toString());
        }
    }

    /** Встраивает текст, сохраняет BMP и показывает маску изменений. */
    private void embedAndSave() {
        if (originalImage == null) {
            showError("Ошибка", new IllegalStateException("Сначала выберите изображение."));
            return;
        }
        if (view.getOutputPath().isBlank()) {
            showError("Ошибка", new IllegalStateException("Не выбран выходной файл."));
            return;
        }

        try {
            EmbedResult result = lsbService.embed(originalImage, view.getMessage());
            Path output = imageService.ensureBmpExtension(Path.of(view.getOutputPath()));
            if (output.toAbsolutePath().normalize().equals(Path.of(view.getInputPath()).toAbsolutePath().normalize())) {
                Alert question = new Alert(Alert.AlertType.CONFIRMATION,
                        "Выбран исходный файл. Перезаписать его?");
                if (question.showAndWait().isEmpty()
                        || question.getResult() != javafx.scene.control.ButtonType.OK) {
                    return;
                }
            }
            imageService.saveBmp(result.getStegoImage(), output);

            view.showChangedBitsMask(imageService.toFxImage(result.getChangedBitsMask()));
            view.setStatusText(
                    "Сохранено. Текст: " + result.getMessageBytes()
                            + " байт, изменено каналов: " + result.getChangedChannels()
            );

            LOGGER.info("Сохранен {}, размер сообщения {} байт", output, result.getMessageBytes());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Готово");
            alert.setHeaderText(null);
            alert.setContentText("Текст встроен, BMP сохранен.");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Ошибка встраивания", e);
        }
    }

    /** Обновляет подпись с текущей вместимостью изображения. */
    private void updateCapacity() {
        if (originalImage == null) {
            view.setCapacityText("Сначала выберите BMP");
            return;
        }

        int bytes = view.getMessage().getBytes(StandardCharsets.UTF_8).length;
        view.setCapacityText(
                "Текст: " + bytes + " байт / максимум: " + lsbService.capacityBytes(originalImage) + " байт"
        );
    }

    /** Создает диалог выбора BMP. */
    private FileChooser createBmpChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP (*.bmp)", "*.bmp"));
        return chooser;
    }

    /** Логирует ошибку и показывает ее пользователю. */
    private void showError(String title, Exception e) {
        LOGGER.error(title, e);
        view.setStatusText(e.getMessage());

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
