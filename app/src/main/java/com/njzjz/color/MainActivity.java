package com.njzjz.color;
/*
*用于通过识别颜色，检测DPA浓度。
*by 华东师范大学 曾晋哲
*/

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private SurfaceView surfaceView;
    private GoogleApiClient client;
    private boolean cameraexist;
    private Bitmap bitmapraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) this.findViewById(R.id.camera_surfaceview);
        if (surfaceView != null) {
            surfaceView.getHolder()
                    .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            surfaceView.getHolder().setFixedSize(176, 144);
            surfaceView.getHolder().setKeepScreenOn(true);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        camera = Camera.open(0);
                        camera.setDisplayOrientation(90);
                        Camera.Parameters parameters = camera.getParameters();
                        int PreviewWidth = 0;
                        int PreviewHeight = 0;
                        List<Camera.Size> size_list = parameters.getSupportedPreviewSizes();
                        if (size_list.size() > 0) {
                            for (Camera.Size cur : size_list) {
                                if (cur.width >= PreviewWidth && cur.height >= PreviewHeight) {
                                    PreviewWidth = cur.width;
                                    PreviewHeight = cur.height;
                                }
                            }
                        }
                        parameters.setPreviewSize(PreviewWidth, PreviewHeight);
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        int w_screen = dm.widthPixels;
                        int h_screen = dm.heightPixels;
                        double rate_h = 1;
                        if (h_screen > PreviewHeight) {
                            rate_h = (double) h_screen / (double) PreviewHeight;
                        }
                        double rate_w = 1;
                        if (w_screen > PreviewWidth) {
                            rate_h = (double) w_screen / (double) PreviewWidth;
                        }
                        double rate = rate_h > rate_w ? rate_h : rate_w;
                        int w = (int) (rate * PreviewWidth);
                        int h = (int) (rate * PreviewHeight);
                        android.widget.RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w, h);
                        surfaceView.setLayoutParams(layoutParams);
                        List<Camera.Size> picSizeValues = camera.getParameters().getSupportedPictureSizes();
                        int picw = 0;
                        int pich = 0;
                        for (Camera.Size size : picSizeValues) {
                            if (size.width > picw && size.height > pich) {
                                picw = size.width;
                                pich = size.height;
                            }
                        }
                        parameters.setPictureSize(picw, pich);
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(holder);
                        cameraexist = true;
                        camera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (camera != null) {
                            camera.release();
                            camera = null;
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format,
                                           int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    camera.stopPreview();
                    if (camera != null) {
                        camera.release();
                        camera = null;
                    }
                }
            });
            surfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (camera != null && cameraexist) {
                                camera.autoFocus(null);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            final int x = (int) event.getX();
                            final int y = (int) event.getY();
                            if (camera != null && cameraexist) {
                                camera.takePicture(null, null, new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        if (Environment.getExternalStorageState().equals(
                                                Environment.MEDIA_MOUNTED)) {
                                            try {
                                                Bitmap bm0 = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                Matrix m = new Matrix();
                                                m.setRotate(90, (float) bm0.getWidth() / 2, (float) bm0.getHeight() / 2);
                                                final Bitmap bitmap = Bitmap.createBitmap(bm0, 0, 0, bm0.getWidth(), bm0.getHeight(), m, true);
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                File jpgFile = new File(Environment
                                                        .getExternalStorageDirectory(), System
                                                        .currentTimeMillis() + ".jpg");
                                                FileOutputStream outputStream = new FileOutputStream(
                                                        jpgFile);
                                                outputStream.write(baos.toByteArray());
                                                outputStream.close();
                                                int sh = surfaceView.getHeight();
                                                int sw = surfaceView.getWidth();
                                                ImageView imageview2 = (ImageView) findViewById(R.id.imgaeview2);
                                                imageview2.setVisibility(View.VISIBLE);
                                                imageview2.setImageBitmap(bitmap);
                                                surfaceView.setVisibility(View.GONE);
                                                getrgb(bitmap, x, y, sh, sw);
                                                Button button3 = (Button) findViewById(R.id.button3);
                                                button3.setVisibility(View.VISIBLE);
                                                cameraexist = false;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "noSD", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            break;
                    }
                    return true;
                }

                ;
            });
        }

        final ImageView imageView2 =(ImageView) findViewById(R.id.imgaeview2);
        imageView2.setVisibility(View.GONE);
        imageView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!cameraexist) {
                            final int x = (int) event.getRawX();
                            final int y = (int) event.getRawY();
                            Bitmap bitmap;
                            if(bitmapraw==null) {
                                imageView2.buildDrawingCache();
                                bitmap = imageView2.getDrawingCache();
                                bitmapraw=bitmap;
                            }else {
                                bitmap = bitmapraw;
                            }
                            int sh = imageView2.getHeight();
                            int sw = imageView2.getWidth();
                            getrgb(bitmap, x, y, sh, sw);
                            Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                            Canvas tempCanvas = new Canvas(tempBitmap);
                            Paint mPaint=new Paint();
                            mPaint.setAntiAlias(false);
                            mPaint.setColor(Color.WHITE);
                            mPaint.setStrokeWidth((float) 3.0);
                            mPaint.setStyle(Paint.Style.STROKE);
                            tempCanvas.drawBitmap(bitmap,0,0,null);
                            tempCanvas.drawCircle(x, y, Integer.parseInt(PreferenceUtils.getPrefString(getApplicationContext(), "size", "50")),mPaint);
                            imageView2.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                        }
                        break;
                }
                return true;
            }
        });
        Button button=(Button)findViewById(R.id.button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1001);
                }
            });
        }
        final Button button3=(Button)findViewById(R.id.button3);
        if (button3 != null) {
            button3.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ImageView imageview2 = (ImageView) findViewById(R.id.imgaeview2);
                   if (imageview2 != null) {
                       imageview2.setVisibility(View.GONE);
                   }
                   surfaceView.setVisibility(View.VISIBLE);
                   button3.setVisibility(View.GONE);
                   cameraexist = true;
                   camera.startPreview();
                   bitmapraw = null;
               }
           });
        }
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this,SettingsActivity.class) );
                }
            }
        );
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (1001 == requestCode && Activity.RESULT_OK == resultCode && null != data) {
            Uri selectImageUri = data.getData();
            String[] filePathColumn = new String[]{MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String pirPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                    Bitmap bitmap = BitmapFactory.decodeFile(pirPath);
                    ImageView imageview2=(ImageView)findViewById(R.id.imgaeview2);
                    if (imageview2 != null) {
                        imageview2.setVisibility(View.GONE);
                        imageview2.setVisibility(View.VISIBLE);
                        imageview2.setImageBitmap(bitmap);
                    }
                    surfaceView.setVisibility(View.GONE);
                    Button button3 = (Button) findViewById(R.id.button3);
                    if (button3 != null) {
                        button3.setVisibility(View.VISIBLE);
                    }
                    cameraexist=false;
                    bitmapraw=null;
                }
            }
            cursor.close();
        }
    }

    public void getrgb(Bitmap bitmap,int x,int y,int sh,int sw){
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();
        int color;
        int xc=x * w / sw;
        int yc=y * h / sh;
        long rs=0,bs=0,gs=0,as=0,n=0;
        int r= Integer.parseInt(PreferenceUtils.getPrefString(getApplicationContext(), "size", "50"));
        for(int i=Math.max(r-xc,0);i<Math.min(xc+r,w)-(xc-r);i++){
            for(int j=Math.max(r-yc,0);j<Math.min(yc+r,h)-(yc-r);j++){
                if(i*i+j*j<=r*r) {
                    color = bitmap.getPixel(xc - r + i, yc - r + j);
                    rs += Color.red(color);
                    gs += Color.green(color);
                    bs += Color.blue(color);
                    as += Color.alpha(color);
                    n += 1;
                }
            }
        }
        int ra=(int)(rs/n);
        int ga=(int)(gs/n);
        int ba=(int)(bs/n);
        int aa=(int)(as/n);
        double k=0.0078,b=0.2496;
        double c=((double)ra/(double)ga-b)/k;
        ImageView imageview = (ImageView) findViewById(R.id.imgaeview);
        if (imageview != null) {
            imageview.setBackgroundColor(Color.argb(aa, ra, ga, ba));
        }
        TextView textview = (TextView) findViewById(R.id.textView);
        if (textview != null) {
            textview.setText(String.format(getResources().getString(R.string.result),ra,ga,ba,c));
        }
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName(getResources().getString(R.string.app_name))
                .setUrl(Uri.parse("http://www.njzjz.win/"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

}
