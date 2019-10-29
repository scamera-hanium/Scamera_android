package com.example.davichiar.scamera_android.BarcodeSearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.davichiar.scamera_android.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;


public class CameraPreview2 extends AppCompatActivity {

    SurfaceView surfaceView;
    boolean flashState = false;
    CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview2);

        surfaceView = findViewById(R.id.cameraView);
        createCameraSource();
    }

    private void toggleFlashLight() {
        android.hardware.Camera camera = null;
        camera = getCamera(cameraSource);
        if (camera != null){
            try{
                android.hardware.Camera.Parameters parameters = camera.getParameters();

                if (!flashState){
                    parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_ON);
                    camera.setParameters(parameters);
                    flashState = !flashState;

                }else {
                    parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);                    camera.setParameters(parameters);
                    camera.setParameters(parameters);
                    flashState = !flashState;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static android.hardware.Camera getCamera(CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields){
            if (field.getType() == android.hardware.Camera.class){
                field.setAccessible(true);
                try{
                    android.hardware.Camera camera = (android.hardware.Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;

    }

    private void createCameraSource() {
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024)
                .build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }

        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                if (barcodeSparseArray.size()>0)
                {
                    Log.d("Array Size", String.valueOf(barcodeSparseArray.size()));
                    Log.d("Data", "Value: " + barcodeSparseArray.valueAt(0).rawValue);

                    Intent intent = new Intent();
                    intent.putExtra("Raw Value",barcodeSparseArray.valueAt(0).rawValue);
                    intent.putExtra("Barcode",barcodeSparseArray.valueAt(0));
                    setResult(CommonStatusCodes.SUCCESS,intent);
                    finish();
                }

            }
        });
    }
}
