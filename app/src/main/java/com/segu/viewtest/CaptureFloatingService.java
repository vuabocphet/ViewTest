package com.segu.viewtest;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.segu.viewtest.capture.AsyncTaskCompat;

import java.nio.ByteBuffer;

public class CaptureFloatingService extends Service implements android.view.View.OnTouchListener {

    private WindowManager mWindowManager;
    private android.view.View mChatHeadView;
    private ConstraintLayout constraintLayout;
    private float xDown = 0, yDown = 0, xUp = 0, yUp = 0;
    private Canvas canvasDrawingPane;
    private Bitmap bitmapDrawingPane;
    private float offsetX, offsetY;
    private ImageView imageDrawingPane, img_touch;
    private WindowManager.LayoutParams params;
    private Bitmap bm=null;
    private View view;

    private static MainActivity context;

    public static void setContext(MainActivity contexts) {
        context = contexts;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDisplay();
        createImageReader();


        params = params(0);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.view, null);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.e("size", width + "-" + height);
        constraintLayout = mChatHeadView.findViewById(R.id.abc);
        imageDrawingPane = mChatHeadView.findViewById(R.id.drawingpane);
        img_touch = mChatHeadView.findViewById(R.id.img_touch);
        view = mChatHeadView.findViewById(R.id.views);
        bitmapDrawingPane = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888);
        canvasDrawingPane = new Canvas(bitmapDrawingPane);
        imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        constraintLayout.setOnTouchListener(this);
        img_touch.setOnTouchListener(onTouchListener);


    }

    private int flags() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private WindowManager.LayoutParams params(int type) {

        return type == 1 ? new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                flags(),
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT) :
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        flags(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
    }

    private android.view.View.OnTouchListener onTouchListener = new android.view.View.OnTouchListener() {

        private int lastAction;
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(android.view.View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //remember the initial position.
                    initialX = params.x;
                    initialY = params.y;
                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    lastAction = event.getAction();
                    return true;
                case MotionEvent.ACTION_UP:


                    if ((Math.abs(event.getRawX() - initialTouchX) < 5) && (Math.abs(event.getRawY() - initialTouchY) < 5)) {

                        startCapture();

                        view.setVisibility(View.VISIBLE);

                        img_touch.setOnTouchListener(null);

                        img_touch.setVisibility(android.view.View.GONE);

                        constraintLayout.setVisibility(android.view.View.VISIBLE);

                        params = params(1);

                        mWindowManager.removeView(mChatHeadView);

                        mWindowManager.addView(mChatHeadView, params);
                    }

                    lastAction = event.getAction();

                    return true;
                case MotionEvent.ACTION_MOVE:

                    params.x = initialX + (int) (event.getRawX() - initialTouchX);

                    params.y = initialY + (int) (event.getRawY() - initialTouchY);

                    mWindowManager.updateViewLayout(mChatHeadView, params);

                    lastAction = event.getAction();

                    return true;

            }

            return false;

        }
    };

    @Override
    public boolean onTouch(android.view.View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                xDown = event.getX();
                yDown = event.getY();

                break;

            case MotionEvent.ACTION_MOVE:

                xUp = event.getX();
                yUp = event.getY();

                drawOnRectProjectedBitMap();

                break;
            case MotionEvent.ACTION_UP:

                xUp = event.getX();
                yUp = event.getY();

                drawOnRectProjectedBitMap();

                view.setVisibility(View.GONE);

                img_touch.setOnTouchListener(onTouchListener);

                img_touch.setVisibility(View.VISIBLE);

                constraintLayout.setVisibility(View.GONE);

                params = params(0);

                mWindowManager.removeView(mChatHeadView);

                mWindowManager.addView(mChatHeadView, params);



                try {
                    context.img.setImageBitmap(setCropBitmap(bm));
                } catch (Exception e) {
                    Log.e("Lỗi set crop bit map", e.toString());
                    e.printStackTrace();
                }

                break;
        }


        return true;
    }

    private void drawOnRectProjectedBitMap() {
        canvasDrawingPane.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6f);
        canvasDrawingPane.drawRect(xDown, yDown, xUp, yUp, mPaint);
        constraintLayout.invalidate();
    }


    //Capture

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    private void getDisplay() {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private static Intent mResultData = null;


    private ImageReader mImageReader;

    public static void setmResultData(Intent mResultData) {
        CaptureFloatingService.mResultData = mResultData;
    }

    private void startScreenShot() {

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                startVirtual();
            }
        }, 5);


    }

    private void createImageReader() {

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startCapture() {

        try {
            Image image = mImageReader.acquireLatestImage();

            if (image == null) {
                startScreenShot();
            } else {
                SaveTask mSaveTask = new SaveTask();
                AsyncTaskCompat.executeParallel(mSaveTask, image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            image.close();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //预览图片
            if (bitmap != null) {
                //TODO BITMAP ĐƯỢC TRẢ VỀ SAU KHI CHỤP
                if (bitmap != null) {
                    bm = bitmap;
                } else {
                    Toast.makeText(CaptureFloatingService.this, "Lỗi to rồi", Toast.LENGTH_SHORT).show();
                }
                //constraintLayout.setVisibility(android.view.View.GONE);
            }
        }
    }

    private Bitmap setCropBitmap(Bitmap bitmaps) {
        try {
            return applyCrop(bitmaps, (int) xDown, (int) yDown, (int) xUp, (int) yUp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    public Bitmap applyCrop(Bitmap bitmap, int x, int y, int xUp, int yUp) throws Exception {

        if (xUp <= 0 || yUp <= 0) {
            return null;
        }
        int w = 0;
        int h = 0;

        if (xUp - x <= 0 && yUp - y <= 0) {
            w = x - xUp;
            h = y - yUp;
            Log.e("W-H", w + "-" + h);
            return Bitmap.createBitmap(bitmap, xUp, yUp, w + 1, h + 1);
        }
        if (xUp - x > 0 && yUp - y > 0) {
            w = xUp - x;
            h = yUp - y;
            Log.e("W-H-A", w + "-" + h);
            return Bitmap.createBitmap(bitmap, x, y, w + 1, h + 1);
        }
        if (xUp - x <= 0 && yUp - y > 0) {
            w = x - xUp;
            h = y;
            Log.e("W-H-B", w + "-" + h);
            return Bitmap.createBitmap(bitmap, xUp, y, w + 1, h + 1);
        }
        if (xUp - x >= 0 && yUp - y < 0) {
            w = xUp - x;
            h = y - yUp;
            Log.e("W-H-C", w + "-" + h);
            return Bitmap.createBitmap(bitmap, x, yUp, w + 1, h + 1);
        }


        return null;
    }


}
