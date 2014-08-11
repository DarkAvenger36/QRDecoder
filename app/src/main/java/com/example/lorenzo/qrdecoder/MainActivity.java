package com.example.lorenzo.qrdecoder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.com.google.zxing.BarcodeFormat;
import main.java.com.google.zxing.BinaryBitmap;
import main.java.com.google.zxing.ChecksumException;
import main.java.com.google.zxing.FormatException;
import main.java.com.google.zxing.MyBinaryBitmap;
import main.java.com.google.zxing.NotFoundException;
import main.java.com.google.zxing.RGBLuminanceSource;
import main.java.com.google.zxing.RGBSource;
import main.java.com.google.zxing.Reader;
import main.java.com.google.zxing.Result;
import main.java.com.google.zxing.ResultPoint;
import main.java.com.google.zxing.common.HybridBinarizer;
import main.java.com.google.zxing.common.RGBHybridBinarizer;
import main.java.com.google.zxing.common.RGBufferedImageLuminanceSource;
import main.java.com.google.zxing.common.SimpleHybridBinarizer;
import main.java.com.google.zxing.hccqrcode.HCCQRcodeReader;
import main.java.com.google.zxing.qrcode.QRCodeReader;


public class MainActivity extends Activity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final int FILE_SELECT_CODE = 0;
    private static final int TAKE_PICTURE = 1;
    private TextView message = null;
    private ImageView imageView = null;
    private static Uri imageUri;
    private static final int SCALEW = 800;
    private static final int SCALEH = 600;
    private Bitmap bmp;


    public void openFilePicker(View view) {


            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Select a image to decode"),
                        FILE_SELECT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(this, R.string.activity_not_found,
                        Toast.LENGTH_SHORT).show();
            }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(LOG_TAG, "File Uri: " + uri.toString());
                    try {
                        // Get the path
                        String path = FileUtils.getPath(this, uri);
                        Log.d(LOG_TAG, "File Path: " + path);

                        decodeImage(path);

                    }catch (URISyntaxException e){
                        e.printStackTrace();
                    }

                }
                break;
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    //Log.d(LOG_TAG, "Uri in resoult = " + selectedImage.toString());

                    decodeImage(selectedImage.getPath());

                }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public static class Global{
        public static  String text = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message = (TextView)findViewById(R.id.myText);
        imageView = (ImageView)findViewById(R.id.imageView);
        message.setMovementMethod(new ScrollingMovementMethod());

    }


    public void decodeImage(String path){

        message.setText("");

        // Get the dimensions of the View
        int targetW = SCALEW;
        int targetH = SCALEH;

        Log.d(LOG_TAG, "W= " + targetW + " H= " + targetH);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Log.d(LOG_TAG, "PATH = " + path);
        bmp = BitmapFactory.decodeFile(path, bmOptions);
        Log.d(LOG_TAG, "dopo il decode");

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0 , width, 0, 0, width, height);
        //RGBufferedImageLuminanceSource src = new RGBufferedImageLuminanceSource(bmp);

        TimingLogger timingLogger = new TimingLogger("TopicLogTag", "decode");

        RGBSource src = new RGBSource(width, height, pixels);

        //BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(src));
        //Reader reader = new QRCodeReader();
        //MyBinaryBitmap bitmap = new MyBinaryBitmap(new SimpleHybridBinarizer(src));
        MyBinaryBitmap bitmap = new MyBinaryBitmap(new RGBHybridBinarizer(src));
        HCCQRcodeReader reader = new HCCQRcodeReader();
        //Log.d(LOG_TAG, "prima del try");

        try{
            Result result = reader.decode(bitmap);

            timingLogger.addSplit("Dopo il decode");
            timingLogger.dumpToLog();

            Global.text = result.getText();
            //byte[] rawBytes = result.getRawBytes();
            //BarcodeFormat format = result.getBarcodeFormat();
            //ResultPoint[] points = result.getResultPoints();

            message.setText(Global.text);
            Log.d(LOG_TAG, "messaggio = " + Global.text);
        }catch (NotFoundException e){
            e.printStackTrace();
        }catch (ChecksumException ce){
            ce.printStackTrace();
        }catch (FormatException fe){
            fe.printStackTrace();
        }

        imageView.setImageBitmap(bmp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_photo) {
            takePhoto();
            return true;
        }
        if (id == R.id.action_choose_file) {
            openFilePicker(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }





    public void takePhoto() {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QRDECODE";
        File dir = new File(file_path);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.d(LOG_TAG, "Sono nell'if");
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(file_path,  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        Log.d(LOG_TAG, "URI = " + imageUri.toString());
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        message.setText(Global.text);
        imageView.setImageBitmap(bmp);
    }
}
