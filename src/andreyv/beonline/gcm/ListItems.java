package andreyv.beonline.gcm;

import android.content.Context;

public class ListItems extends TwoLineArrayAdapter<TwoLine> {
    public ListItems(Context context, TwoLine[] employees) {
        super(context, employees);
    }

    @Override
    public String lineOneText(TwoLine t) {
        return t.time;
    }

    @Override
    public String lineTwoText(TwoLine t) {
        return t.title;
    }
}
