package andreyv.beonline.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.google.android.gcm.GCMConstants;

/**
 * Обработчик ошибки регистрации приложения. При неудачной попытке регистрации приложения ресивер принимает сообщение
 * со случайным числом и пытается повторно зарегистрироваться в GCM
 * @see 'http://megadarja.blogspot.ru/2012/12/google-cloud-messaging.html'
 */
public class GCMRetryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        GCMHelper session = GCMHelper.getInstance(context);

        if (GCMConstants.INTENT_FROM_GCM_LIBRARY_RETRY.equals(action)) {
            /**
             * Если переданный токен повторной регистрации актуален, то есть соответствует сохраненной копии, то
             * продолжим выполнение метода.
             * */
            long expectedRetryToken = session.getRetryToken();
            long actualRetryToken = intent.getLongExtra(GCMHelper.PREF_GCM_RETRY_TOKEN, 0);
            if (expectedRetryToken != actualRetryToken) {
                Log.w("Got invalid retry token, do nothing");
                return;
            }

            /**
             * Получим RegistrationId и если он пустой (регистрации не было), проведем регистрацию, иначе отменим
             * существующую регистрацию (она как-то получилась с ошибкой).
             * */
            String gcmToken = session.getGCMToken();
            if (TextUtils.isEmpty(gcmToken)) {
                Log.i("Retrying last GCM registration");
                session.register();
                session.dialog.show();
            } else {
                Log.i("Retrying last GCM un registration");
                session.unRegister();
            }
        }
    }
}