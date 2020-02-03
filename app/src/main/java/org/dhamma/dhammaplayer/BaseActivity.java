package org.dhamma.dhammaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {
    public interface OnEndOfProgressBarAnimation {
        void takeAction();
    }

    public interface OnPermissionResult {
        void takeAction(boolean result);
    }

    private AlertDialog mAlertDialog;
    private TextView mProgressText;
    private OnPermissionResult mPermissionCallback;

    static public final int PERMISSION_EXT_STORAGE = 1;

    @SuppressLint("SetTextI18n")
    private void createAlertDialog(String progressMessage){
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        mProgressText = new TextView(this);
        mProgressText.setText(progressMessage);
        mProgressText.setTextColor(Color.parseColor("#000000"));
        mProgressText.setTextSize(16);
        mProgressText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(mProgressText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(ll);

        mAlertDialog = builder.create();
    }

    public void showProgressBar(String progressMessage){
        if (null == mAlertDialog) {
            createAlertDialog(progressMessage);
        } else {
            mProgressText.setText(progressMessage);
        }
        mAlertDialog.show();
        Window window = mAlertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(mAlertDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mAlertDialog.getWindow().setAttributes(layoutParams);
        }
    }

    public void hideProgressBar(){
        mAlertDialog.dismiss();
    }

    public void showProgressBarForTime(String message, int seconds, final OnEndOfProgressBarAnimation callback) {
        showProgressBar(message);
        CountDownTimer cdt = new CountDownTimer(seconds*1000, seconds*1000) {
            @Override
            public void onTick(long l) {
                return;
            }

            @Override
            public void onFinish() {
                hideProgressBar();
                callback.takeAction();
            }
        };
        cdt.start();
    }

    public void requestPermission(int requestCode, String requestMessage, OnPermissionResult callback) {
        String request;
        // Map request code to request string.
        switch (requestCode) {
            case PERMISSION_EXT_STORAGE:
                request = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            default:
                callback.takeAction(false);
                return;
        }

        if (ContextCompat.checkSelfPermission(this, request) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, request)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, requestMessage, Toast.LENGTH_SHORT).show();
            }
            // Save the callback function.
            mPermissionCallback = callback;
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{request},
                    requestCode);
        } else {
            // Permission has already been granted
            callback.takeAction( true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_EXT_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionCallback.takeAction(true);
                } else {
                    mPermissionCallback.takeAction(false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.AT_MOST);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
}
