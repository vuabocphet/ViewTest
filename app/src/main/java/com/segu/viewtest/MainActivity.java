package com.segu.viewtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    public ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        CaptureFloatingService.setContext(this);
       requestCapturePermission(this);

    }

    public static final int REQUEST_MEDIA_PROJECTION = 18;

    public static void requestCapturePermission(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        ((Activity) context).startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:

                if (resultCode == RESULT_OK && data != null) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 1234);
                        } else {
                            CaptureFloatingService.setmResultData(data);
                            startService(new Intent(this, CaptureFloatingService.class));
                        }
                    } else {
                        CaptureFloatingService.setmResultData(data);
                        startService(new Intent(this, CaptureFloatingService.class));
                    }
                }

                break;
        }

    }

}
