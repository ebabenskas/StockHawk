package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.StockDetailsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

class StockWidgetRemoteViewsFactory  implements RemoteViewsService.RemoteViewsFactory {
    private Cursor data;
    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    StockWidgetRemoteViewsFactory(Context context) {
        this.context = context;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        Uri stockUri = Contract.Quote.URI;
        data = context.getContentResolver().query(
                stockUri, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews remoteViews =  new RemoteViews(context.getPackageName(),
                               R.layout.list_item_quote);
        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        String price = dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));
        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        if (rawAbsoluteChange > 0) {
            remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        if (PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_percentage_key))) {
            change = percentageFormat.format(percentageChange / 100);
        }
        remoteViews.setTextViewText(R.id.symbol, symbol);
        remoteViews.setTextViewText(R.id.price, price);
        remoteViews.setTextViewText(R.id.change, change);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(StockDetailsActivity.STOCK, symbol);
        remoteViews.setOnClickFillInIntent(R.id.list_item, fillInIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.widget);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position))
            return data.getLong(Contract.Quote.POSITION_ID);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
