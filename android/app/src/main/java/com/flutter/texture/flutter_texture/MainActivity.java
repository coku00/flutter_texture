package com.flutter.texture.flutter_texture;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class MainActivity extends FlutterActivity {
    public static final String TAG = "MainActivity";
    MethodChannel channel;
    private Handler backgroundHandler;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        backgroundHandler = new Handler();
        channel = new MethodChannel(flutterEngine.getDartExecutor()
                .getBinaryMessenger(), "flutter/texture/channel");

        channel.setMethodCallHandler((call, result) -> {
            switch (call.method) {
                case "createTexture":
                    createTexture(flutterEngine,result);
                    break;
            }
        });
    }

    private void createTexture(FlutterEngine flutterEngine,MethodChannel.Result result) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            result.success(-1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
            return;
        }
        TextureRegistry.SurfaceTextureEntry entry =
                flutterEngine.getRenderer().createSurfaceTexture();
        SurfaceTexture surfaceTexture = entry.surfaceTexture();
        Surface surface = new Surface(surfaceTexture);
        try {
            cameraManager.openCamera(
                    "0",
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice device) {
                            result.success(entry.id());
                            CaptureRequest.Builder previewRequestBuilder = null;
                            try {
                                previewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                previewRequestBuilder.addTarget(surface);
                                startPreview(result,device,surface,previewRequestBuilder,backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {

                        }

                        @Override
                        public void onError(@NonNull CameraDevice cameraDevice, int errorCode) {
                            Log.i(TAG, "open | onError");
                            result.success(-1);
                        }
                    },
                    backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private void startPreview(MethodChannel.Result result,CameraDevice device, Surface surface, CaptureRequest.Builder previewRequestBuilder, Handler backgroundHandler) {
        try {
            device.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "startPreview");
                    try {
                        session.setRepeatingRequest(previewRequestBuilder.build(),null,backgroundHandler );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "startPreview Failed");
                }
            },backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
