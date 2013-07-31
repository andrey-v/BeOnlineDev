package andreyv.beonline.gcm;

import android.text.TextUtils;

/**
 * Обертка для лога. Оставлена без изменений.
 * @see 'http://megadarja.blogspot.ru/2012/12/google-cloud-messaging.html'
 */
@SuppressWarnings("ALL")
public class Log {
    private static final String PREFIX = "gcm";

    private static final String TAG = tag("common");

    private static boolean ENABLED = true;
    private static final boolean LOCATION_ENABLED = true;

    public static String tag(String tag) {
        return PREFIX + "-" + tag;
    }

    public static void vt(String tag, String format, Object... args) {
        if (ENABLED) android.util.Log.v(tag, String.format(format, args) + getLocation());
    }

    public static void dt(String tag, String format, Object... args) {
        if (ENABLED) android.util.Log.d(tag, String.format(format, args) + getLocation());
    }

    public static void it(String tag, String format, Object... args) {
        if (ENABLED) android.util.Log.i(tag, String.format(format, args) + getLocation());
    }

    public static void wt(String tag, String format, Object... args) {
        if (ENABLED) android.util.Log.w(tag, String.format(format, args) + getLocation());
    }

    public static void wt(String tag, String message, Throwable e) {
        if (ENABLED) android.util.Log.w(tag, message + getLocation(), e);
    }

    public static void wt(String tag, Throwable e) {
        if (ENABLED) android.util.Log.w(tag, e);
    }

    public static void et(String tag, Throwable e) {
        if (ENABLED) android.util.Log.e(tag, e.getMessage() + getLocation(), e);
    }

    public static void et(String tag, String format, Object... args) {
        if (ENABLED) android.util.Log.e(tag, String.format(format, args) + getLocation());
    }

    public static void v(String format, Object... args) {
        vt(TAG, format, args);
    }

    public static void d(String format, Object... args) {
        dt(TAG, format, args);
    }

    public static void i(String format, Object... args) {
        it(TAG, format, args);
    }

    public static void w(String format, Object... args) {
        wt(TAG, format, args);
    }

    public static void w(String message, Throwable e) {
        wt(TAG, message, e);
    }

    public static void w(Throwable e) {
        wt(TAG, e);
    }

    public static void e(String format, Object... args) {
        et(TAG, format, args);
    }

    public static void e(Throwable e) {
        et(TAG, e);
    }

    private static String getLocation() {
        if (!LOCATION_ENABLED)
            return "";

        final String logClassName = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(logClassName)) {
                        Class<?> clazz = Class.forName(trace.getClassName());

                        String clazzName = clazz.getSimpleName();
                        if (TextUtils.isEmpty(clazzName))
                            clazzName = clazz.getName();

                        return String.format(" [%s.%s:%d]", clazzName, trace.getMethodName(), trace.getLineNumber());
                    }
                } else if (trace.getClassName().startsWith(logClassName)) {
                    found = true;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return " []";
    }
}
