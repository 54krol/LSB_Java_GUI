package lsb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lsb.SteganographyException;
import lsb.EmbedResult;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты LSB-алгоритма. */
class LsbSteganographyServiceTest {

    private LsbSteganographyService service;

    /** Создает сервис перед тестом. */
    @BeforeEach
    void setUp() {
        service = new LsbSteganographyService();
    }

    /** Проверяет запись и обратное чтение текста. */
    @Test
    void textCanBeWrittenAndReadBack() throws Exception {
        BufferedImage image = createImage(100, 100);
        String text = "Привет, LSB! 123";

        EmbedResult result = service.embed(image, text);
        assertEquals(text, service.extract(result.getStegoImage()));
    }

    /** Несколько коротких сообщений, включая пустое и с русскими буквами. */
    @Test
    void differentMessagesCanBeExtracted() {
        Stream.of("", "abc", "Привет", "Тест 123", "Символы !?", "🙂")
                .forEach(text -> {
                    try {
                        BufferedImage image = createImage(60, 60);
                        assertEquals(text, service.extract(service.embed(image, text).getStegoImage()));
                    } catch (SteganographyException e) {
                        throw new AssertionError(e);
                    }
                });
    }

    /** Проверяет, что меняются только младшие биты. */
    @Test
    void onlyLsbCanChange() throws Exception {
        BufferedImage image = createImage(80, 80);
        BufferedImage result = service.embed(image, "Проверка").getStegoImage();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int before = image.getRGB(x, y);
                int after = result.getRGB(x, y);

                assertEquals((before >>> 16) & 0xFE, (after >>> 16) & 0xFE);
                assertEquals((before >>> 8) & 0xFE, (after >>> 8) & 0xFE);
                assertEquals(before & 0xFE, after & 0xFE);
            }
        }
    }

    /** Проверяет ошибку при слишком большом сообщении. */
    @Test
    void tooLongTextCausesException() {
        BufferedImage image = createImage(2, 2);
        assertThrows(SteganographyException.class,
                () -> service.embed(image, "Этот текст сюда не поместится"));
    }

    /** Проверяет расчет вместимости. */
    @Test
    void capacityIsCalculatedCorrectly() {
        BufferedImage image = createImage(20, 20);
        int expected = (20 * 20 * 3 - 32) / 8;
        assertEquals(expected, service.capacityBytes(image));
    }

    /** Проверяет значения RGB в маске. */
    @Test
    void maskContainsZeroOr255() throws Exception {
        BufferedImage mask = service.embed(createImage(50, 50), "mask").getChangedBitsMask();

        for (int y = 0; y < mask.getHeight(); y++) {
            for (int x = 0; x < mask.getWidth(); x++) {
                int rgb = mask.getRGB(x, y);
                int r = (rgb >>> 16) & 0xFF;
                int g = (rgb >>> 8) & 0xFF;
                int b = rgb & 0xFF;
                assertTrue((r == 0 || r == 255) && (g == 0 || g == 255) && (b == 0 || b == 255));
            }
        }
    }

    /** Создает простую тестовую картинку. */
    private BufferedImage createImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = (x * 3 + y) & 0xFF;
                int g = (x + y * 5) & 0xFF;
                int b = (x * 7 + y * 11) & 0xFF;
                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }
}
