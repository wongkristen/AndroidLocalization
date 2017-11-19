package com.kristenwong.localizationanddailypath;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

/**
 * Created by kristenwong on 11/19/17.
 */

public class CheckInListAdapter extends BaseAdapter {
    private List<CheckIn> mCheckIns;
    private LayoutInflater mInflater;

    public CheckInListAdapter(List<CheckIn> checkIns, Context context) {
        mCheckIns = checkIns;
        mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mCheckIns.size();
    }

    @Override
    public Object getItem(int i) {
        return mCheckIns.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView name, lat, lon, address, time;
        CheckIn checkIn = mCheckIns.get(i);

        View v = mInflater.inflate(R.layout.checkin_list_item, viewGroup, false);

        name = (TextView) v.findViewById(R.id.text_list_item_checkin_name);
        lat = (TextView) v.findViewById(R.id.text_list_item_lat);
        lon = (TextView) v.findViewById(R.id.text_list_item_lon);
        address = (TextView) v.findViewById(R.id.text_list_item_address);
        time = (TextView) v.findViewById(R.id.text_list_item_time);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(4);

        name.setText(checkIn.getName());
        lat.setText(decimalFormat.format(checkIn.getLatitude()));
        lon.setText(decimalFormat.format(checkIn.getLongitude()));
        address.setText(checkIn.getAddress());
        time.setText(checkIn.getTime());

        return v;
    }

    public void updateList(List<CheckIn> checkIns) {
        mCheckIns = checkIns;
        notifyDataSetChanged();
    }
}
