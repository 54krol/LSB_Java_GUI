package lsb.service;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import lsb.SteganographyException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Загрузка, сохранение и небольшие преобразования изображений. */
public class ImageService {

    /** Загружает BMP с диска. */
    public BufferedImage loadBmp(Path path) throws SteganographyException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new SteganographyException("Файл не найден.");
        }
        if (!path.getFileName().toString().toLowerCase().endsWith(".bmp")) {
            throw new SteganographyException("Нужно выбрать BMP-файл.");
        }

        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                throw new SteganographyException("Не удалось прочитать изображение.");
            }
            return toRgb(image);
        } catch (IOException e) {
            throw new SteganographyException("Ошибка чтения BMP: " + e.getMessage());
        }
    }

    /** Сохраняет изображение в формате BMP. */
    public void saveBmp(BufferedImage image, Path path) throws SteganographyException {
        try {
            Path absolute = path.toAbsolutePath();
            if (absolute.getParent() != null) {
                Files.createDirectories(absolute.getParent());
            }

            if (!ImageIO.write(image, "bmp", absolute.toFile())) {
                throw new SteganographyException("BMP writer не найден.");
            }
        } catch (IOException e) {
            throw new SteganographyException("Ошибка сохранения BMP: " + e.getMessage());
        }
    }

    /** Преобразует BufferedImage в JavaFX Image. */
    public Image toFxImage(BufferedImage image) {
        return SwingFXUtils.toFXImage(image, null);
    }

    /** Делает стандартное имя выходного файла. */
    public Path createDefaultOutputPath(Path input) {
        String name = input.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return input.resolveSibling(name + "_stego.bmp");
    }

    /** Добавляет расширение .bmp, если его нет. */
    public Path ensureBmpExtension(Path path) {
        if (path.toString().toLowerCase().endsWith(".bmp")) {
            return path;
        }
        return Path.of(path + ".bmp");
    }

    /** Приводит изображение к обычному RGB без альфа-канала. */
    private BufferedImage toRgb(BufferedImage source) {
        BufferedImage result = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = result.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return result;
    }
}
