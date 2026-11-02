package lsb;

import java.awt.image.BufferedImage;

/** Результат работы LSB-алгоритма. */
public class EmbedResult {

    private final BufferedImage stegoImage;
    private final BufferedImage changedBitsMask;
    private final int messageBytes;
    private final long changedChannels;

    /** Создает объект с результатами встраивания. */
    public EmbedResult(BufferedImage stegoImage, BufferedImage changedBitsMask,
                       int messageBytes, long changedChannels) {
        this.stegoImage = stegoImage;
        this.changedBitsMask = changedBitsMask;
        this.messageBytes = messageBytes;
        this.changedChannels = changedChannels;
    }

    /** Возвращает изображение со скрытым текстом. */
    public BufferedImage getStegoImage() {
        return stegoImage;
    }

    /** Возвращает маску измененных младших битов. */
    public BufferedImage getChangedBitsMask() {
        return changedBitsMask;
    }

    /** Возвращает размер сообщения в байтах. */
    public int getMessageBytes() {
        return messageBytes;
    }

    /** Возвращает число измененных RGB-каналов. */
    public long getChangedChannels() {
        return changedChannels;
    }
}
