package andreyv.beonline.gcm;

import android.content.Context;
import android.content.SharedPreferences;

public class GCMList {

    /**
     * Контекст исполнения
     */
    private Context mContext;

    /**
     * Приватные настройки приложения с ключем 'session'
     */
    private SharedPreferences mPreferences;

    private static volatile GCMList mInstance;

    public static final String MSG_COUNT = "msg_count";
    public static final String MSG = "msg_content_";

    public static GCMList getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GCMList.class) {
                if (mInstance == null) {
                    mInstance = new GCMList(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * Констркутор класса
     */
    private GCMList(Context context) {
        /** Контекст приложения */
        mContext = context.getApplicationContext();
        /** Приватные настройки приложения с ключем 'session' */
        mPreferences = mContext.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public synchronized Integer getMessageCount() {
        Integer msgCount = mPreferences.getInt(MSG_COUNT, 0);
        Log.d("Stored messages count: [%s]", msgCount);
        return msgCount;
    }

    public synchronized void saveMessageCount(Integer msgCount) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(MSG_COUNT, msgCount);
        editor.commit();
    }

    public synchronized void saveMsg(String msg, String header, String time) {
        Integer msgCount = this.getMessageCount() + 1;
        String sMsgCount = msgCount.toString();

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(MSG + sMsgCount, msg);
        editor.putString(MSG + "header_" + sMsgCount, header);
        editor.putString(MSG + "time_" + sMsgCount, time);
        this.saveMessageCount(msgCount);

        editor.commit();
    }

    public synchronized ListItems getAdapter() {
        Integer msgCount = this.getMessageCount();
        TwoLine[] msgHeaders = new TwoLine[msgCount];

        for (Integer i = msgCount; i >= 1; i--) {
            String msgHeader = mPreferences.getString(MSG + "header_" + i.toString(), "error");
            String msgTime = mPreferences.getString(MSG + "time_" + i.toString(), "error");
            msgHeaders[msgCount - i] = new TwoLine();
            msgHeaders[msgCount - i].title = msgHeader;
            msgHeaders[msgCount - i].time = msgTime;
        }

        return new ListItems(mContext, msgHeaders);
    }

    public synchronized void clearMsg() {
        Integer msgCount = this.getMessageCount();
        String sMsgCount = msgCount.toString();
        SharedPreferences.Editor editor = mPreferences.edit();

        for (Integer i = 1; i <= msgCount; i++) {
            editor.remove(MSG + sMsgCount);
            editor.remove(MSG + "header_" + sMsgCount);
            editor.remove(MSG + "time_" + sMsgCount);
        }
        this.saveMessageCount(0);
    }

    public synchronized String getMsg(Integer index) {
        Integer msgCount = this.getMessageCount(); //1 2 3 4
        index = msgCount - index; // Т.к. сообщения идут в обратном порядке
        return mPreferences.getString(MSG + index.toString(), "error");
    }

}
