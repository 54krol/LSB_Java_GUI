package lsb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Представление главного окна. Класс только создает элементы интерфейса и отдает их контроллеру. */
public class MainView {

    private final BorderPane root = new BorderPane();
    private final TextField inputPathField = new TextField();
    private final TextField outputPathField = new TextField();
    private final TextArea messageArea = new TextArea();
    private final Button chooseInputButton = new Button("Выбрать BMP");
    private final Button chooseOutputButton = new Button("Куда сохранить");
    private final Button embedButton = new Button("Встроить текст и сохранить");
    private final Label capacityLabel = new Label("Сначала выберите BMP");
    private final Label statusLabel = new Label("Готов к работе");
    private final ImageView originalImageView = createImageView();
    private final ImageView maskImageView = createImageView();

    /** Создает главное окно программы. */
    public MainView() {
        configureFields();
        root.setPadding(new Insets(12));
        root.setTop(createControls());
        root.setCenter(createPreviewPane());
        root.setBottom(createStatusPane());
    }

    /** Возвращает корневой элемент для Scene. */
    public Parent getRoot() {
        return root;
    }

    /** Возвращает кнопку выбора исходного BMP. */
    public Button getChooseInputButton() {
        return chooseInputButton;
    }

    /** Возвращает кнопку выбора выходного BMP. */
    public Button getChooseOutputButton() {
        return chooseOutputButton;
    }

    /** Возвращает кнопку запуска встраивания. */
    public Button getEmbedButton() {
        return embedButton;
    }

    /** Возвращает поле сообщения. */
    public TextArea getMessageArea() {
        return messageArea;
    }

    /** Возвращает введенный путь исходного файла. */
    public String getInputPath() {
        return inputPathField.getText();
    }

    /** Показывает путь выбранного исходного BMP. */
    public void setInputPath(String path) {
        inputPathField.setText(path);
    }

    /** Возвращает введенный путь выходного файла. */
    public String getOutputPath() {
        return outputPathField.getText();
    }

    /** Показывает путь выходного BMP. */
    public void setOutputPath(String path) {
        outputPathField.setText(path);
    }

    /** Возвращает текст для встраивания. */
    public String getMessage() {
        return messageArea.getText();
    }

    /** Показывает исходное изображение. */
    public void showOriginalImage(Image image) {
        originalImageView.setImage(image);
    }

    /** Показывает маску реально измененных младших битов. */
    public void showChangedBitsMask(Image image) {
        maskImageView.setImage(image);
    }

    /** Очищает правое окно с маской. */
    public void clearChangedBitsMask() {
        maskImageView.setImage(null);
    }

    /** Обновляет строку вместимости изображения. */
    public void setCapacityText(String text) {
        capacityLabel.setText(text);
    }

    /** Обновляет строку состояния. */
    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    /** Настраивает текстовые поля. */
    private void configureFields() {
        inputPathField.setEditable(false);
        outputPathField.setEditable(false);
        messageArea.setPromptText("Введите текст, который нужно спрятать в BMP");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
    }

    /** Создает верхнюю панель с путями, текстом и кнопками. */
    private VBox createControls() {
        GridPane paths = new GridPane();
        paths.setHgap(8);
        paths.setVgap(8);
        paths.add(new Label("Исходный BMP:"), 0, 0);
        paths.add(inputPathField, 1, 0);
        paths.add(chooseInputButton, 2, 0);
        paths.add(new Label("Выходной BMP:"), 0, 1);
        paths.add(outputPathField, 1, 1);
        paths.add(chooseOutputButton, 2, 1);
        GridPane.setHgrow(inputPathField, Priority.ALWAYS);
        GridPane.setHgrow(outputPathField, Priority.ALWAYS);

        VBox controls = new VBox(8, paths, new Label("Текст для встраивания:"), messageArea);
        controls.setPadding(new Insets(0, 0, 12, 0));
        return controls;
    }

    /** Создает центральную область предпросмотра. */
    private HBox createPreviewPane() {
        VBox originalBox = createImageBox("Оригинал", originalImageView);
        VBox maskBox = createImageBox("Только измененные LSB", maskImageView);
        HBox preview = new HBox(12, originalBox, maskBox);
        HBox.setHgrow(originalBox, Priority.ALWAYS);
        HBox.setHgrow(maskBox, Priority.ALWAYS);
        return preview;
    }

    /** Создает нижнюю строку управления. */
    private VBox createStatusPane() {
        HBox actionRow = new HBox(12, capacityLabel, embedButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(capacityLabel, Priority.ALWAYS);

        VBox bottom = new VBox(6, actionRow, statusLabel);
        bottom.setPadding(new Insets(12, 0, 0, 0));
        return bottom;
    }

    /** Создает контейнер с подписью и изображением. */
    private VBox createImageBox(String title, ImageView imageView) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setPrefViewportHeight(390);

        VBox box = new VBox(6, label, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return box;
    }

    /** Создает ImageView с одинаковыми параметрами масштабирования. */
    private ImageView createImageView() {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);
        imageView.setFitWidth(500);
        imageView.setFitHeight(390);
        return imageView;
    }
}
