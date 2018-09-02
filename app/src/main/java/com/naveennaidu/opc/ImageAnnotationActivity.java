package com.naveennaidu.opc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ImageAnnotationActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    ImageView annotationImageView;
    Button saveButton;
    Button undoButton;

    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    Paint maskPaint;
    private Path path = new Path();

    Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    Bitmap cropBitmap;
    Bitmap cbmp;
    Bitmap resultMaskBitmap;
    Bitmap getMaskBitmap;
    Bitmap finalBitmap;

    int maxValX;
    int maxValY;
    int minValX;
    int minValY;

    Bitmap maskBmp;

    Uri photoUri;
    ArrayList<Point> drawPoints = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Image Annotation");
        setContentView(R.layout.activity_annotation_image);

        annotationImageView = findViewById(R.id.annotationImageView);
        saveButton = findViewById(R.id.saveButton);
        undoButton = findViewById(R.id.undoButton);

        saveButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);

        initDrawCanvas();
    }


    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            if (alteredBitmap != null) {

                FileOutputStream fileOutputStream = null;
                File file = new File(photoUri.getPath());
                try {
                    fileOutputStream = new FileOutputStream(file);
                    alteredBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                maxValX = 0;
                maxValY = 0;

                for (int i=0; i<drawPoints.size(); i++){
                    Log.e("point", ""+drawPoints.get(i).x);
                    if (drawPoints.get(i).x > maxValX) {
                        maxValX = drawPoints.get(i).x;
                    }
                    if (drawPoints.get(i).y > maxValY) {
                        maxValY = drawPoints.get(i).y;
                    }
                }
                minValX = maxValX;
                minValY = maxValY;
                for (int i=0; i<drawPoints.size(); i++){
                    if (drawPoints.get(i).x < minValX){
                        minValX = drawPoints.get(i).x;
                    }
                    if (drawPoints.get(i).y < minValY){
                        minValY = drawPoints.get(i).y;
                    }
                }

                performCrop(photoUri);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (view == undoButton) {
            initDrawCanvas();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = motionEvent.getX();
                downy = motionEvent.getY();
                path.moveTo(downx, downy);
                Log.e("down points", ""+downx + "," + downy);
                break;
            case MotionEvent.ACTION_MOVE:
                upx = motionEvent.getX();
                upy = motionEvent.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                annotationImageView.invalidate();
                downx = upx;
                downy = upy;
                drawPoints.add(new Point(Math.round(upx) , Math.round(upy)));
                path.lineTo(upx, upy);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    private void initDrawCanvas(){
        photoUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        try {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);

            bmpFactoryOptions.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, bmpFactoryOptions);

            alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
            Log.e("canvas", "" + canvas.getWidth() + "-" + bmp.getWidth() + "," + canvas.getHeight() + "-" + bmp.getHeight());
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);

            matrix = new Matrix();
            canvas.drawBitmap(bmp, matrix, paint);

            annotationImageView.setImageBitmap(alteredBitmap);
            annotationImageView.setOnTouchListener(this);
        } catch (Exception e) {
            Log.v("ERROR", e.toString());
        }
    }

    private void performCrop(Uri uri){

        //drawn bitmap
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        try {
            cbmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        try {
            cbmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //mask bitmap
        maskBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        maskBmp.eraseColor(Color.TRANSPARENT);
        Canvas maskCanvas = new Canvas(maskBmp);
        maskPaint = new Paint();
        maskPaint.setColor(Color.rgb(255,255,255));
        maskPaint.setStyle(Paint.Style.FILL);
        maskCanvas.drawPath(path, maskPaint);

        finalBitmap = stackMaskingProcess(cbmp, maskBmp);


        cropBitmap = Bitmap.createBitmap(finalBitmap, minValX-5, minValY-5, maxValX-minValX+10, maxValY-minValY+10);
        FileOutputStream fileOutputStream = null;

        String photoUriString = photoUri.toString();
        String[] splitUri = photoUriString.split("/");
        Random random = new Random();
        String croppedUri = Environment.getExternalStorageDirectory() + "/" + splitUri[6]+ "/" + splitUri[7]+ "/" + splitUri[7] + random.nextInt(100) +  "_cropped.png";

        File file = new File(Uri.parse(croppedUri).getPath());
        try {
            fileOutputStream = new FileOutputStream(file);
            cropBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap stackMaskingProcess(Bitmap _originalBitmap, Bitmap _maskingBitmap) {
        try {
            if (_originalBitmap != null)
            {
                int intWidth = _originalBitmap.getWidth();
                int intHeight = _originalBitmap.getHeight();
                resultMaskBitmap = Bitmap.createBitmap(intWidth, intHeight, Bitmap.Config.ARGB_8888);
                getMaskBitmap = Bitmap.createScaledBitmap(_maskingBitmap, intWidth, intHeight, true);
                Canvas mCanvas = new Canvas(resultMaskBitmap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mCanvas.drawBitmap(_originalBitmap, 0, 0, null);
                mCanvas.drawBitmap(getMaskBitmap, 0, 0, paint);
                paint.setXfermode(null);
                paint.setStyle(Paint.Style.STROKE);
            }
        } catch (OutOfMemoryError o) {
            o.printStackTrace();
        }
        return resultMaskBitmap;
    }
}
