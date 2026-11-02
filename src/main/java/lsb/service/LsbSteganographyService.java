package lsb.service;

import lsb.SteganographyException;
import lsb.EmbedResult;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

/** Работа с текстом, спрятанным в младших битах RGB-каналов. */
public class LsbSteganographyService {

    private static final int LENGTH_BITS = 32;
    private static final int CHANNELS = 3;

    /** Встраивает UTF-8 текст в копию изображения. */
    public EmbedResult embed(BufferedImage source, String text) throws SteganographyException {
        if (source == null) {
            throw new SteganographyException("Изображение не выбрано.");
        }

        byte[] message = text.getBytes(StandardCharsets.UTF_8);
        long needBits = LENGTH_BITS + (long) message.length * 8;
        long haveBits = capacityBits(source);

        if (needBits > haveBits) {
            throw new SteganographyException(
                    "Текст не помещается. Нужно " + needBits + " бит, доступно " + haveBits + "."
            );
        }

        BufferedImage result = copyImage(source);
        BufferedImage mask = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB
        );

        int totalBits = (int) needBits;
        int bitIndex = 0;
        long changed = 0;

        for (int y = 0; y < result.getHeight() && bitIndex < totalBits; y++) {
            for (int x = 0; x < result.getWidth() && bitIndex < totalBits; x++) {
                int[] rgb = unpackRgb(result.getRGB(x, y));
                int maskColor = 0;

                // Записываем данные по очереди в R, G, B.
                for (int c = 0; c < CHANNELS && bitIndex < totalBits; c++) {
                    int oldValue = rgb[c];
                    int bit = getPayloadBit(message, bitIndex);
                    rgb[c] = (oldValue & 0xFE) | bit;
                    bitIndex++;

                    if (oldValue != rgb[c]) {
                        changed++;
                        maskColor |= 255 << (16 - c * 8);
                    }
                }

                result.setRGB(x, y, packRgb(rgb));
                mask.setRGB(x, y, maskColor);
            }
        }

        return new EmbedResult(result, mask, message.length, changed);
    }

    /** Извлекает текст из изображения, созданного этим приложением. */
    public String extract(BufferedImage image) throws SteganographyException {
        if (image == null || capacityBits(image) < LENGTH_BITS) {
            throw new SteganographyException("Изображение слишком маленькое.");
        }

        int length = 0;
        for (int i = 0; i < LENGTH_BITS; i++) {
            length = (length << 1) | readBit(image, i);
        }

        if (length < 0 || length > capacityBytes(image)) {
            throw new SteganographyException("Некорректная длина скрытого сообщения.");
        }

        byte[] message = new byte[length];
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            int value = 0;
            for (int bit = 0; bit < 8; bit++) {
                int index = LENGTH_BITS + byteIndex * 8 + bit;
                value = (value << 1) | readBit(image, index);
            }
            message[byteIndex] = (byte) value;
        }

        return new String(message, StandardCharsets.UTF_8);
    }

    /** Считает, сколько байт текста помещается в изображение. */
    public int capacityBytes(BufferedImage image) {
        long textBits = capacityBits(image) - LENGTH_BITS;
        if (textBits <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, textBits / 8);
    }

    /** Возвращает очередной бит длины или самого сообщения. */
    private int getPayloadBit(byte[] message, int bitIndex) {
        if (bitIndex < LENGTH_BITS) {
            return (message.length >>> (31 - bitIndex)) & 1;
        }

        int messageBit = bitIndex - LENGTH_BITS;
        int byteIndex = messageBit / 8;
        int bitInByte = 7 - messageBit % 8;
        return ((message[byteIndex] & 0xFF) >>> bitInByte) & 1;
    }

    /** Читает один LSB по номеру RGB-канала. */
    private int readBit(BufferedImage image, int bitIndex) {
        int pixelIndex = bitIndex / CHANNELS;
        int channel = bitIndex % CHANNELS;
        int x = pixelIndex % image.getWidth();
        int y = pixelIndex / image.getWidth();
        int[] rgb = unpackRgb(image.getRGB(x, y));
        return rgb[channel] & 1;
    }

    /** Возвращает общую емкость изображения в битах. */
    private long capacityBits(BufferedImage image) {
        return (long) image.getWidth() * image.getHeight() * CHANNELS;
    }

    /** Создает независимую RGB-копию изображения. */
    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB
        );
        copy.setData(source.getData());
        return copy;
    }

    /** Разбирает цвет пикселя на R, G и B. */
    private int[] unpackRgb(int rgb) {
        return new int[]{
                (rgb >>> 16) & 0xFF,
                (rgb >>> 8) & 0xFF,
                rgb & 0xFF
        };
    }

    /** Собирает R, G и B обратно в int. */
    private int packRgb(int[] rgb) {
        return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }
}
