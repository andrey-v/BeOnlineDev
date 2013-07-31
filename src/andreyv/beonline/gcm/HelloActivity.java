package andreyv.beonline.gcm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Главная активность, открывается при старте программы
 */
public class HelloActivity extends TrackedActivity {
    /**
     * Сессия хелпера GCM
     */
    private GCMHelper mSession;
    /**
     * Сессия хелпера списка собощений
     */
    private GCMList mSessionList;

    public AlertDialog dialog;

    /**
     * Событе создания текущей активности. Здесь определяется какой слой будет отображаться, если ппользователь
     * авторизован на сайте, то список его сообщений, если нет, то форму авторизации.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /** Вызываем родительское событие, поскольку метод переопределен */
        super.onCreate(savedInstanceState);


        /** Инициализируем синглетоны сессий */
        mSession = GCMHelper.getInstance(this);
        mSessionList = GCMList.getInstance(this);

        /** Получаем сохраненный ранее токен авторизации, полученный с сервера, если авторзации не было - пустая строка */
        String serverToken = mSession.getServerToken();

        if (mSession.getGCMToken() == null) {
            gcmRegisterNotify();
        }

        if (serverToken.equals("")) {
            /** Нужно авторизоваться на сайте - откроем форму авторизации */
            setContentView(R.layout.main);
        } else {
            /** Пользователь уже аввторизован - откроем список сообщений */
            setContentView(R.layout.list);
            refreshList();  // и обновим его

            /** Создадим листнер нажатия на элементе списка сообщений */
            AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {

                /**
                 * Обработчик события нажатия на элементе списка сообщений
                 *
                 * @param adapterView Адаптер списка
                 * @param view Текущее представление
                 * @param position Позиция нажатого элемента
                 * @param id Его Id (у нас совпадает с position)
                 */
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    /** Отобразим автивность с текстом сообщения стандартным способом */
                    Intent intent = new Intent(HelloActivity.this, PushActivity.class);
                    intent.putExtra(Constants.KEY_MESSAGE_BODY, mSessionList.getMsg(position));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            };
            /** Назначим созданный листнер */
            ListView lv = (ListView) findViewById(R.id.listView);
            if (lv.getOnItemClickListener() == null)
                lv.setOnItemClickListener(itemListener);
        }
    }

    private void gcmRegisterNotify() {

        String title = "Соглашение об использовании приложения";
        String message = "Это приложение будет присылать Вам push-сообщения с сайта GladCode.ru" +
                ", содержащие информацию о событиях сайта. Если Вы согласны использовать это приложение " +
                "выберите 'Да я согласен', иначе 'Нет'.";
        String button1String = "Да я согласен";
        String button2String = "Нет";

        AlertDialog.Builder ad = new AlertDialog.Builder(HelloActivity.this);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setCancelable(false);
        // установка кнопок и слушателей для них
        ad.setPositiveButton(button1String, onClickListener_DialogResetPin);
        ad.setNegativeButton(button2String, new OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                closeApp();
            }
        });
        ad.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                closeApp();
            }
        });

        dialog = ad.create();
        dialog.show();

        Button btnOK = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOK.setOnClickListener(onClickListener_btnOK);
    }

    View.OnClickListener onClickListener_btnOK = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (hasInternetConnection()) {
                mSession.dialog.show();
                mSession.register();
                mSession.dialog.dismiss();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Соединение с сервером отсутствует. Включите соединение с интернет и повторите операцию еще раз!",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        return nInfo != null && nInfo.isConnected();
    }

    // слушатель нажатия на кнопки "ОК" и "Отмена"
    DialogInterface.OnClickListener onClickListener_DialogResetPin =
            new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // При нажатии кнопки "Отмена" выполнится этот метод,
                    // но при нажатии кнопки "ОК" он не выполнится,
                    // так как мы изменили слушателя для кнопки "ОК"
                }
            };

    private void closeApp() {
        moveTaskToBack(true);
        finish();
        System.runFinalizersOnExit(true);
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        closeApp();
    }



    public void onDestroy() {
        super.onDestroy();
        System.runFinalizersOnExit(true);
        System.exit(0);
    }

    /**
     * Обработчик авторизации пользователя и получения GCM-токена
     *
     * @param view Текущее представление
     */
    @SuppressWarnings("UnusedParameters")
    public void gcmRegister(View view) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        /** Зарегистрируемся в GCM, получим токен */
        registerOnServer();
    }

    public void registerOnServer() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String gcmToken = mSession.getGCMToken();

        /** Получим элементы ввода логина и пароля */
        EditText login = (EditText) findViewById(R.id.login);
        EditText pass = (EditText) findViewById(R.id.editText1);

        /** Получим текст логина и md5 пароля */
        String sLogin = login.getText().toString();
        String sPassword = md5(pass.getText().toString());

        /** Отправляем логин и пароль на сервер */
        RequestTask rt = new RequestTask(HelloActivity.this);
        rt.execute(Constants.SITE_NAME + "beonline/login", sLogin, sPassword, gcmToken);
    }

    /**
     * Кодируем в md5
     *
     * @param passwd Пароль пользователя
     * @return String
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String md5(String passwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(passwd.getBytes("UTF-8"));
        BigInteger hash = new BigInteger(1, md.digest());

        return hash.toString(16);
    }

    /**
     * Отмена регистрации в GCM
     */
    @SuppressWarnings("UnusedParameters")
    public void gcmUnRegister() {
        mSession.unRegister();
    }

    /**
     * То, что делаем прикаждом старте активности
     */
    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }

    /**
     * Перестраивает список сообщений
     */
    public void refreshList() {
        ListItems adapter = mSessionList.getAdapter();
        ListView lv = (ListView) findViewById(R.id.listView);
        if (lv != null)
            lv.setAdapter(adapter);
    }

    /**
     * Очищает список сообщений - обработчик кнопки очистки
     *
     * @param view Текущее представление
     */
    @SuppressWarnings("UnusedParameters")
    public void clearList(View view) {
        mSessionList.clearMsg();
        refreshList();
    }

    /**
     * Выход пользователя с сайта, отмена регистрации на сайте
     *
     * @param view Текущее представление
     */
    @SuppressWarnings("UnusedParameters")
    public void exit(View view) {
        if (hasInternetConnection()) {
            mSession.deleteServerToken();
            gcmUnRegister();
            setContentView(R.layout.main);
            closeApp();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Для того, что бы отменить прием сообщений об этом нужно сообщить серверу. " +
                            "Включите соединение с интернет и повторите операцию еще раз!",
                    Toast.LENGTH_LONG).show();
        }
    }

}