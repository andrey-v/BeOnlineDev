package andreyv.beonline.gcm;

import android.*;
import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T> {
    private int mListItemLayoutResId;

    public TwoLineArrayAdapter(Context context, T[] ts) {
        this(context, android.R.layout.two_line_list_item, ts);
    }

    public TwoLineArrayAdapter(
            Context context,
            int listItemLayoutResourceId,
            T[] ts) {
        super(context, listItemLayoutResourceId, ts);
        mListItemLayoutResId = listItemLayoutResourceId;
    }

    @Override
    public android.view.View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inFlater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItemView = convertView;
        if (null == convertView) {
            listItemView = inFlater.inflate(mListItemLayoutResId, parent, false);
        }

        TextView lineOneView = (TextView) listItemView.findViewById(R.id.text1);
        TextView lineTwoView = (TextView) listItemView.findViewById(R.id.text2);

        T t = (T) getItem(position);
        lineOneView.setText(lineOneText(t));
        lineTwoView.setText(lineTwoText(t));

        return listItemView;
    }

    public abstract String lineOneText(T t);

    public abstract String lineTwoText(T t);
}

