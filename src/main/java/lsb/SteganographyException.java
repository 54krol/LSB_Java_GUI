package lsb;

/** Ошибка при работе со стеганографией или BMP. */
public class SteganographyException extends Exception {

    /** Создает исключение с сообщением для пользователя. */
    public SteganographyException(String message) {
        super(message);
    }
}
