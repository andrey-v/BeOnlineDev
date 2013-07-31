package andreyv.beonline.gcm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMConstants;

/**
 * Класс, обрабатывающий получаемое от сервера сообщение
 *
 * @see 'http://megadarja.blogspot.ru/2012/12/google-cloud-messaging.html'
 */
public class GCMIntentService extends GCMBaseIntentService {

    /**
     * Метод получения сообщения. Он принимает сообщение. отправляет ее в новую активность и выводит уведомление
     * пользователю.
     *
     * @param context Context
     * @param intent  Intent
     */
    @Override
    protected void onMessage(Context context, Intent intent) {
        /** Получим переданные данные */
        Log.v("Message received, [%s]", intent.getAction());
        Bundle extras = intent.getExtras();
        String messageHeader = extras.getString(Constants.KEY_MESSAGE_HEADER);
        Log.v("Extras keys: [%s]", TextUtils.join(", ", extras.keySet().toArray(new String[extras.keySet().size()])));
        Log.v("Message: [%s]", messageHeader);

        GCMList mSession = GCMList.getInstance(this);
        mSession.saveMsg(
                extras.getString(Constants.KEY_MESSAGE_BODY),
                extras.getString(Constants.KEY_MESSAGE_HEADER),
                extras.getString(Constants.KEY_MESSAGE_TIME)
        );

        /** Создадим экземпляр активности для приема сообщения и передадим в нее пришедшие данные*/
        Intent pushIntent = new Intent(context, PushActivity.class);
        pushIntent.putExtras(intent);

        /** Создадим ожидающую активность */
        PendingIntent pi = PendingIntent.getActivity(context, 0, pushIntent, 0);

        /** Создадим уведомление и закрепим за ним активность */
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification(R.drawable.ic_stat_example, messageHeader, System.currentTimeMillis());
        n.setLatestEventInfo(context, messageHeader, "", pi);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        /** Отобразим сообщение */
        n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        nm.notify(R.id.push_notification, n);
    }

    /**
     * Обработка ошибки регистрации приложения в GCM. Смысл обработчика следующий: при получении ошибки мы отправляем
     * широковещательное сообщение со случайным числом (не забываем разрешить его в AndroidManifest) и с указанным
     * в настройках временем задержки. Это сообщение потом ловим ресивером GCMRetryReceiver и там регистрируемся
     * повторно. Если опять произошла ошибка, то сработает этот же метод, но с увеличенным в два раза интервалом
     * ожидания повторной регистрации.
     *
     * @param context Context
     * @param errorId String
     */
    @Override
    protected void onError(Context context, String errorId) {
        Log.e("Error received [%s]", errorId);
        /** При отсутствии сервиса или ошибке аутентификации на нем обработаем ошибку */
        if (GCMConstants.ERROR_SERVICE_NOT_AVAILABLE.equals(errorId) || GCMConstants.ERROR_AUTHENTICATION_FAILED.equals(errorId)) {
            GCMHelper gcm = GCMHelper.getInstance(context);

            long backOffTimeMs = gcm.getBackOffTime(); // Получение отсрочки из Shared Preferences
            long retryToken = gcm.generateRetryToken(); // Получение случайного числа и сохранение его в Shared Preferences

            long nextAttempt = SystemClock.elapsedRealtime() + backOffTimeMs; // Время следующей попытки регистрации
            /** Создадим интент и поместим в него полученное ранее и сохраненное в настройках сучайное число */
            Intent retryIntent = new Intent(GCMConstants.INTENT_FROM_GCM_LIBRARY_RETRY);
            retryIntent.putExtra(GCMHelper.PREF_GCM_RETRY_TOKEN, retryToken);

            /** Установим оповещение с отсрочкой в backOffTimeMs */
            PendingIntent retryPendingIntent = PendingIntent.getBroadcast(context, 0, retryIntent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME, nextAttempt, retryPendingIntent);

            /** Для следующей обработки ошибки активации увеличим время отсрочки в два раза */
            gcm.updateBackOff(backOffTimeMs * 2);

            Toast.makeText(
                    context,
                    "Нет соединения с сервером сообщений. Подключитесь к Интернет и повторите попытку!",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Событие, срабатываемое при успешной регистрации приложения
     *
     * @param context        Context
     * @param registrationId String
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.v("Push token registered in GCM [%s]", registrationId);

        /** Получим токен и сохраним его */
        GCMHelper gcm = GCMHelper.getInstance(context);
        gcm.saveGCMToken(registrationId);
        /** Очистим таймаут отсрочки повторной регистрации при удачно прошедшей процедуре */
        gcm.clearBackOff();

        GCMHelper mSession = GCMHelper.getInstance(context);
        mSession.dialog.dismiss();
        ((HelloActivity)mSession.context).dialog.dismiss();

    }

    /**
     * Событие, срабатываемое при отмене регистрации приложения
     *
     * @param context        Context
     * @param registrationId String
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.v("Push token un registration [%s]", registrationId);

        if (TextUtils.isEmpty(registrationId)) {
            return;
        }

        GCMHelper gcm = GCMHelper.getInstance(context);
        gcm.removeGCMToken();
        gcm.clearBackOff();

        // @todo Удалить токен с сервера
    }

    @Override
    protected String[] getSenderIds(Context context) {
        return new String[]{
                Constants.GCM_SENDER_ID
        };
    }
}
