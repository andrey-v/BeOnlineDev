package andreyv.beonline.gcm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gcm.GCMRegistrar;

import java.util.Random;

/**
 * Хелпер
 *
 * @see 'http://megadarja.blogspot.ru/2012/12/google-cloud-messaging.html'
 */
public class GCMHelper {
    /**
     * Тайм-аут повтора получения токена при ошибке регистрации
     */
    private static final long DEFAULT_BACK_OFF = 10000;
    /**
     * Контекст исполнения
     */
    private Context mContext;
    public Context context;

    /**
     * Приватные настройки приложения с ключем 'session'
     */
    private SharedPreferences mPreferences;

    ProgressDialog dialog;
    /**
     * Ключи сохраненных неастроек
     */
    private static final String PREF_GCM_TOKEN = "gcm_token";
    private static final String PREF_GCM_BACK_OFF = "back_off";
    public static final String PREF_GCM_RETRY_TOKEN = "retry_token";
    public static final String PREF_SERVER_TOKEN = "server_token";

    private static volatile GCMHelper mInstance;

    public static GCMHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GCMHelper.class) {
                if (mInstance == null) {
                    mInstance = new GCMHelper(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * Констркутор класса
     */
    private GCMHelper(Context context) {
        /** Контекст приложения */
        this.context = context;
        mContext = context.getApplicationContext();
        /** Приватные настройки приложения с ключем 'session' */
        mPreferences = mContext.getSharedPreferences("session", Context.MODE_PRIVATE);

        dialog = new ProgressDialog(context);
        dialog.setMessage("Соединение с сервером сообщений...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
    }

    /**
     * Метод регистрации приложения в GCM и получения пользовательского токена.
     */
    public void register() {
        GCMRegistrar.checkDevice(mContext);
        GCMRegistrar.checkManifest(mContext);
        String pushToken = GCMRegistrar.getRegistrationId(mContext);
        Log.v("RegistrationId=[%s]", pushToken);
        if (pushToken.equals("")) {
            GCMRegistrar.register(mContext, Constants.GCM_SENDER_ID);
        } else {
            Log.w("Already registered");
        }
    }

    /**
     * Отмена регистрации приложения в GCM
     */
    public void unRegister() {
        GCMRegistrar.unregister(mContext);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREF_GCM_TOKEN);
    }

    /**
     * Сохранение токена в сохраняемые настройки
     *
     * @param gcmToken Значение сохраняемого токена
     */
    public synchronized void saveGCMToken(String gcmToken) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_GCM_TOKEN, gcmToken);
        editor.commit();
    }

    /**
     * Сохранение токена сервера в сохраняемые настройки
     *
     * @param serverToken Значение сохраняемого токена
     */
    public synchronized void saveServerToken(String serverToken) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_SERVER_TOKEN, serverToken);
        editor.commit();
    }

    public synchronized void deleteServerToken() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_SERVER_TOKEN, "");
        editor.commit();
    }

    /**
     * Удаление токена из созраняемых настроек
     */
    public synchronized void removeGCMToken() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREF_GCM_TOKEN);
        editor.commit();
    }

    /**
     * Получение токена из ранее сохраненных настроек приложения
     *
     * @return string Токен приложения
     */
    public String getGCMToken() {
        String gcmToken = mPreferences.getString(PREF_GCM_TOKEN, null);
        Log.d("Stored GCM token: [%s]", gcmToken);
        return gcmToken;
    }

    /**
     * Получение токена сервера из ранее сохраненных настроек приложения
     *
     * @return string Токен приложения
     */
    public String getServerToken() {
        String serverToken = mPreferences.getString(PREF_SERVER_TOKEN, "");
        Log.d("Stored GCM token: [%s]", serverToken);
        return serverToken;
    }

    /**
     * Получение времени тайм-аута повтора получения токена при ошибке регистрации
     *
     * @return long
     */
    public long getBackOffTime() {
        return mPreferences.getLong(PREF_GCM_BACK_OFF, DEFAULT_BACK_OFF);
    }

    /**
     * Сохранение времени тайм-аута повтора получения токена при ошибке регистрации
     *
     * @param backOff long
     */
    public void updateBackOff(long backOff) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(PREF_GCM_BACK_OFF, backOff);
        editor.commit();
    }

    /**
     * Удаление сохраненного значения времени тайм-аута повтора получения токена при ошибке регистрации
     */
    public void clearBackOff() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREF_GCM_BACK_OFF);
        editor.commit();
    }

    /**
     * Получения токена повторной регистрации
     *
     * @return long
     */
    public long generateRetryToken() {
        long retryToken = new Random(System.currentTimeMillis()).nextLong();

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(PREF_GCM_RETRY_TOKEN, retryToken);
        editor.commit();

        return retryToken;
    }

    /**
     * Получение токена повторной регистрации
     *
     * @return long
     */
    public long getRetryToken() {
        return mPreferences.getLong(PREF_GCM_RETRY_TOKEN, 0);
    }


}
