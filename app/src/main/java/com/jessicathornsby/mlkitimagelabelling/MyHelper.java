package com.jessicathornsby.mlkitimagelabelling;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.database.Cursor;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.LinearLayout;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.net.Uri;
import android.provider.Settings;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.BitmapFactory.decodeFile;
import static android.graphics.BitmapFactory.decodeStream;

public class MyHelper {
    private static Dialog mDialog;

    public static String getPath(Context context, Uri uri) {
        String path = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    public static File createTempFile(File file) {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/com.example.mlkit");
        if (!dir.exists() || !dir.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        if (file == null) {
            file = new File(dir, "original.jpg");
        }
        return file;
    }

    public static void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.addContentView(
                new ProgressBar(context),
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        mDialog.setCancelable(false);
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public static void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public static void needPermission(final Activity activity, final int requestCode, int msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(msg);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, requestCode);
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setCancelable(false);
        alert.show();
    }

    public static Bitmap resizeImage(File imageFile, Context context, Uri uri, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int photoW = options.outWidth;
            int photoH = options.outHeight;

            options.inSampleSize = Math.min(photoW / view.getWidth(), photoH / view.getHeight());
            return compressImage(imageFile, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap resizeImage(File imageFile, String path, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(path, options);

        int photoW = options.outWidth;
        int photoH = options.outHeight;

        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.min(photoW / view.getWidth(), photoH / view.getHeight());
        return compressImage(imageFile, BitmapFactory.decodeFile(path, options));
    }

    private static Bitmap compressImage(File imageFile, Bitmap bmp) {
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }
}
