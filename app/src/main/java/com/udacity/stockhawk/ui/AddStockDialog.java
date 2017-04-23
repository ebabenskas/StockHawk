package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.loader.StockCheckLoader;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Boolean> {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private static final int CHECK_LOADER = 33;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }
        });
        builder.setView(custom);
        builder.setMessage(getString(R.string.dialog_title));
        builder.setPositiveButton(getString(R.string.dialog_add), null);
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        Dialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideError();
                    addStock();
                }
            });
            positiveButton.setContentDescription(getString(R.string.add_stock));
            Button cancelButton = d.getButton(Dialog.BUTTON_NEGATIVE);
            cancelButton.setContentDescription(getString(R.string.cancel_stock));
        }
    }

    private void addStock() {
        String symbol = stock.getText().toString();
        if (symbol.isEmpty())
            showEmptyError();
        if (!Utils.networkUp(getActivity())) {
            showNetworkError();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(StockCheckLoader.STOCK, symbol);
        startLoader(CHECK_LOADER, bundle, this);
    }


    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new StockCheckLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean exist) {
        if (exist) {
            hideError();
            Activity parent = getActivity();
            if (parent instanceof MainActivity) {
                ((MainActivity) parent).addStock(stock.getText().toString());
            }
            dismissAllowingStateLoss();
        } else showNotExistError();

    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
    }

    private void showEmptyError() {
        error.setText(R.string.error_empty);
        error.setVisibility(View.VISIBLE);
    }

    private void showNotExistError() {
        error.setText(R.string.error_not_exist);
        error.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        error.setText(R.string.error_network);
        error.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        error.setVisibility(View.GONE);
    }

    private void startLoader(int loaderId, Bundle bundle, LoaderManager.LoaderCallbacks callbacks) {
        LoaderManager loaderManager = getMainActivity().getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(loaderId);
        if (loader == null) {
            loaderManager.initLoader(loaderId, bundle, callbacks);
        } else {
            loaderManager.restartLoader(loaderId, bundle, callbacks);
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
