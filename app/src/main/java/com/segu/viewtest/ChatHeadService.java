package com.segu.viewtest;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ChatHeadService extends Service implements View.OnTouchListener {

    private WindowManager mWindowManager;
    private android.view.View mChatHeadView;
    private Canvas canvasDrawingPane;
    private Bitmap bitmapDrawingPane;
    private float offsetX, offsetY;
    private float xDown = 0, yDown = 0, xUp = 0, yUp = 0;
    private WindowManager.LayoutParams params;
    private ConstraintLayout constraintLayout;
    private ImageView imageDrawingPane;
    private Bitmap bm;

    public ChatHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.layout_chat_head, null);

        //Add the view to the window.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);

        constraintLayout = mChatHeadView.findViewById(R.id.cs_layout);
        imageDrawingPane = mChatHeadView.findViewById(R.id.drawingpane);
        bitmapDrawingPane = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888);
        canvasDrawingPane = new Canvas(bitmapDrawingPane);
        constraintLayout.setOnTouchListener(this);

        bm = drawableToBitmap(getResources().getDrawable(R.drawable.ic_android_black_24dp));
        offsetX = bm.getWidth() / 2;
        offsetY = bm.getHeight() / 2;
        //Set the close button.
        final RelativeLayout rl_layout = (RelativeLayout) mChatHeadView.findViewById(R.id.rl_layout);

        imageDrawingPane.setOnTouchListener(new android.view.View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                        constraintLayout.setVisibility(View.VISIBLE);
                        // rl_layout.setVisibility(View.GONE);
                        imageDrawingPane.setImageBitmap(bitmapDrawingPane);
                        imageDrawingPane.setOnTouchListener(null);
                        canvasDrawingPane.drawBitmap(bm, initialX, initialY, null);
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        xUp = Math.abs(params.x);
                        yUp = Math.abs(params.y);
                        //Update the layout with new X & Y coordinate
                        Log.e("Param.X", xUp + "");
                        Log.e("Param.Y", yUp + "");
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }

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
                Log.e("X", xDown + "");
                Log.e("Y", yDown + "");
                Log.e("XUP", xUp + "");
                Log.e("YUP", yUp + "");
                // startCapture();
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
        canvasDrawingPane.drawBitmap(bm, xUp - offsetX, yUp - offsetY, null);
        constraintLayout.invalidate();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}