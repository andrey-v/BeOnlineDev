package andreyv.beonline.gcm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, осуществляющий отправку данных авторизации на сервер
 *
 * @see HelloActivity
 */
class RequestTask extends AsyncTask<String, String, String> {
    /**
     * Диалог, который висит, пока данные отправляются
     */
    private ProgressDialog dialog;
    /**
     * Главная активность, устанавливается в конструкторе класса
     */
    private HelloActivity mainActivity;

    /**
     * Констрактор класса
     *
     * @param mainActivity Активность, в которой создан этот класс
     */
    RequestTask(HelloActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void setMainActivity(HelloActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Обработчик отправки данных на сервер
     *
     * @param params массив передаваемых паремтров, фактически используется три - логин, пароль, рег-ид
     * @return String
     */
    @Override
    protected String doInBackground(String... params) {
        String response = "error";
        try {
            //создаем запрос на сервер
            DefaultHttpClient hc = new DefaultHttpClient();
            ResponseHandler<String> res = new BasicResponseHandler();
            //он у нас будет посылать post запрос
            HttpPost postMethod = new HttpPost(params[0]);
            //будем передавать два параметра
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            //лоигн
            nameValuePairs.add(new BasicNameValuePair("login", params[1]));
            //пароль
            nameValuePairs.add(new BasicNameValuePair("password", params[2]));
            //токен
            nameValuePairs.add(new BasicNameValuePair("reg_id", params[3]));
            /** Для параллельной отладки с php-сервером, при необходимости отремить */
            //  nameValuePairs.add(new BasicNameValuePair("XDEBUG_SESSION_START", "12095"));
            //собераем их вместе и посылаем на сервер
            postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //получаем ответ от сервера
            response = hc.execute(postMethod, res);
            // обнулим токен
            GCMHelper session = GCMHelper.getInstance(this.mainActivity);
            session.saveServerToken("");

        } catch (Exception e) {
            System.out.println("Упс...");
        }

        return response;
    }

    /**
     * То. что делаем до отправки - выводим сообщение
     */
    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(this.mainActivity);
        dialog.setMessage("Соединяемся...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
        super.onPreExecute();
    }

    /**
     * То, что делаем после отправки - закрываем сообщение и обрабатываем результат
     *
     * @param result Результат соединения с сервером
     */
    @Override
    protected void onPostExecute(String result) {
        dialog.dismiss();
        super.onPostExecute(result);

        /** Данные не пришли на сервер - это может быть в том случае, если нет соединения с интеретом, отвалился сервер,
         * логин пустой, пароль пустой, GCM-токен не получен (отвалился гугл:)) */
        if (result.equals("")) {
            Toast.makeText(
                    this.mainActivity.getApplicationContext(),
                    "Авторизация не прошла, логин или пароль пустой, а может нет соединения с интернетом!",
                    Toast.LENGTH_LONG).show();
        } else

        /** Нет соединения с интернет или ошибка в коде выполнения запроса*/
            if (result.equals("error")) {
                Toast.makeText(
                        this.mainActivity.getApplicationContext(),
                        "Соединение с сервером отсутствует. Включите соединение с интернет и повторите авторизацию еще раз!",
                        Toast.LENGTH_LONG).show();
            } else {

                /** Сюда попали, если результат с сервера пришел и он в формате json */
                try {
                    /** Создадим читателя json объектов и отдали ему строку - result */
                    JSONObject json = new JSONObject(result);
                    /** Получим переданные данные */
                    String data = json.getString("data");

                    /** Какие-то из параметров запроса на сервер не пришли */
                    if (data.equals("error")) {
                        Toast.makeText(
                                this.mainActivity.getApplicationContext(),
                                "Ошибка непредвиденная случилась на сервере, авторизацию позже еще раз попробуй ты!",
                                Toast.LENGTH_LONG).show();
                    }

                    /** Пользователь зарегистрирован. но не активирован */
                    if (data.equals("error activation")) {
                        Toast.makeText(
                                this.mainActivity.getApplicationContext(),
                                "Ваша учетная запись не активирована. На Ваш почтовый ящик пришло письмо с сылкой на " +
                                        "активацию, воспользуйтесь ей и повторите авторизацию еще раз!",
                                Toast.LENGTH_LONG).show();
                    }

                    /** Указаны ошибочные логин или пароль */
                    if (data.equals("error login")) {
                        Toast.makeText(
                                this.mainActivity.getApplicationContext(),
                                "Логин или пароль указаны ошибочно. Проверьте их еще раз!",
                                Toast.LENGTH_LONG).show();
                    }

                    if (data.equals("ok")) {
                        /** ПОЛЬЗОВАТЕЛЬ АВТОРИЗОВАЛСЯ УСПЕШНО, ОБРАБОТАЕМ ЭТО СЧАСТЛИВОЕ СОБЫТИЕ */
                        /** Если все работает, то этот код написал я, если нет, то я не знаю кто это писал */
                        Toast.makeText(
                                this.mainActivity.getApplicationContext(),
                                "Вы успешно авторизованы на сайте. Скоро Вам начнут приходить уведомления!",
                                Toast.LENGTH_LONG).show();

                        /** Сохраним токен */
                        GCMHelper session = GCMHelper.getInstance(this.mainActivity);
                        String hash = json.getString("hash");
                        session.saveServerToken(hash);

                        /** Обновим активность */
                        Intent intent = new Intent(this.mainActivity, HelloActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        this.mainActivity.startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }
}