package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String STOCK  = "STOCK";
    private static final int DETAILS_LOADER = 1;
    private static final String STATE_STOCK_NAME = "name";
    private static final String STATE_STOCK_HISTORY = "history";

    public static Intent getIntent(Context context, String symbol){
        Intent intent = new Intent(context, StockDetailsActivity.class);
        intent.putExtra(STOCK, symbol);
        return intent;
    }

    @BindView(R.id.stock_chart)
    LineChart lineChart;
    private String stockSymbol;
    private String history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        restoreIfSaved(savedInstanceState);
        if (getIntent().hasExtra(STOCK)){
            stockSymbol = getIntent().getStringExtra(STOCK);
        }
        setTitle(stockSymbol +" " + getString(R.string.history));
        if (history != null)
            drawChart(history);
        else getSupportLoaderManager().initLoader(DETAILS_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(stockSymbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        history = getHistoryAsString(data);
        drawChart(history);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_STOCK_NAME, stockSymbol);
        outState.putString(STATE_STOCK_HISTORY, history);
        super.onSaveInstanceState(outState);
    }

    private class DateValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date date = new Date(Float.valueOf(value).longValue());
            return SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(date);
        }
    }

    private void drawChart(String history) {
        LineDataSet dataSet = new LineDataSet(getData(history),stockSymbol);
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setLineWidth(1);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new DateValueFormatter());
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setText("");
        lineChart.getLegend().setEnabled(false);
    }

    private List<Entry> getData(String history) {
        final LinkedList<Entry> entries = new LinkedList<>();
        CSVReader csvReader = new CSVReader(new StringReader(history));
        try {
            List<String[]> lines = csvReader.readAll();
            for (int i = lines.size() - 1; i >= 0; i--) {
                String[] line = lines.get(i);
                Entry entry = new Entry( Float.parseFloat(line[0]), Float.parseFloat(line[1]));
                entries.add(entry);
            }
        } catch (IOException e) {
            Timber.e(e);
        }
        return entries;
    }

    private String getHistoryAsString(Cursor data) {
        String history = "";
        if(data.moveToFirst()) {
            history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        }
        if(!data.isClosed()){
            data.close();
        }
        return history;
    }

    private void restoreIfSaved(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_STOCK_NAME)){
            stockSymbol = savedInstanceState.getString(STATE_STOCK_NAME);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_STOCK_HISTORY)){
            history = savedInstanceState.getString(STATE_STOCK_HISTORY);
        }
    }

}
