package andreyv.beonline.gcm;

/**
 * Константные значения, используемые в приложении
 * @see 'http://megadarja.blogspot.ru/2012/12/google-cloud-messaging.html'
 */
public class Constants {
    /** Project Number (он же Sender ID) */
    public static final String GCM_SENDER_ID = "?????????????????????????";
    /** Ключ текста получаемого сообщения */
    public static final String KEY_MESSAGE_BODY = "message";
    /** Ключ заголовка получаемого сообщения */
    public static final String KEY_MESSAGE_HEADER = "header";
    /** Ключ времени сообщения */
    public static final String KEY_MESSAGE_TIME = "time";
    /** Адрес сайта (обязательно с / в конце строки)*/
    public static final String SITE_NAME = "http://gladcode.ru/";
}
