package com.udacity.stockhawk.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.udacity.stockhawk.R;

import java.io.IOException;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockCheckLoader extends AsyncTaskLoader<Boolean> {
    private Boolean result;
    private Bundle args;
    public static final String STOCK = "stock";

    public StockCheckLoader(Context context, Bundle args) {
        super(context);
        this.args = args;
    }

    @Override
    protected void onStartLoading() {
        if (args == null) {
            return;
        }
        if (result != null) {
            deliverResult(result);
        } else {
            forceLoad();
        }
    }

    @Override
    public Boolean loadInBackground() {
        String symbol = args.getString(STOCK);
        try {
            Timber.d(getContext().getString(R.string.debug_check_stock) + symbol);
            Stock stock = YahooFinance.get(symbol);
            return stock.getQuote().getPrice() != null;
        } catch (IOException e) {
            Timber.e(getContext().getString(R.string.error_parsing) + e.getMessage());
            return false;
        } catch (Exception e) {
            Timber.e(getContext().getString(R.string.error_not_found) + e.getMessage());
            return false;
        }
    }

    @Override
    public void deliverResult(Boolean result) {
        this.result =result;
        super.deliverResult(result);
    }
}
