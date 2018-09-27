package com.example.asvladimirov.appb;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String uri, uri2;
    private ImageView imageView;

    final String LOG_TAG = "myLogs";

    final Uri CONTACT_URI = Uri
            .parse("content://com.example.asvladimirov.provider.ImageURL/images");

    final String URI = "Uri";
    final String STATUS = "Status";
    final String TIME = "Time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        uri = getIntent().getStringExtra("URI");
        uri2 = getIntent().getStringExtra("URI_2");
        int status = getIntent().getIntExtra("INT_URI_2", 1);
        if(isOnline()) {
            if (uri != null || uri2 != null) {
               if(uri2 == null) {
                   saveURI(1, uri);
                   showImage(uri);
               } else if(uri == null) {
                   if (status == 1) {
                       deleteURI(uri2);
                       showImage(uri2);
                   } else {
                       changeURI(1, uri2);
                       showImage(uri2);
                   }
               }
            } else {
                alert();
            }
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
            saveURI(2, uri);
        }
    }

    private void changeURI(int status, String uri2) {
        ContentValues cv = new ContentValues();
        cv.put(TIME, String.valueOf(Calendar.getInstance().getTime()));
        cv.put(STATUS, status);
        cv.put(URI, uri2);
        getContentResolver().update(CONTACT_URI, cv, URI + " LIKE " + uri2, null);
    }

    private void deleteURI(String uri2) {
        final String uri = uri2;
        new CountDownTimer(15000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                getContentResolver().delete(CONTACT_URI, URI + " LIKE " + uri, null);
            }
        }.start();

    }

    private void saveURI(int status, String uri) {
        ContentValues cv = new ContentValues();
        cv.put(TIME, String.valueOf(Calendar.getInstance().getTime()));
        cv.put(STATUS, status);
        cv.put(URI, uri);
        getContentResolver().insert(CONTACT_URI, cv);
    }

    private void showImage(String ImageUri) {
        BitmapAsync bitmapAsync = new BitmapAsync();
        bitmapAsync.execute(ImageUri);

    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void alert() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("This app is not independent!");
        alertDialog.setMessage("App will be closet at 10");
        alertDialog.show();

        new CountDownTimer(10000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage("App will be closet at " + (millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    @SuppressLint("StaticFieldLeak")
    class BitmapAsync extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap bitmap = null;
            try {
                URL Url = new URL(url[0]);
                URLConnection conn = Url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e("TAG", "Error getting bitmap", e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
}
