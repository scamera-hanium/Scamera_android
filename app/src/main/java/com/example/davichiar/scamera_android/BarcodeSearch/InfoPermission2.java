package com.example.davichiar.scamera_android.BarcodeSearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.davichiar.scamera_android.QRSearch.QRCodeActivity;
import com.example.davichiar.scamera_android.R;

public class InfoPermission2 extends AppCompatActivity {

    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_permission);

        imageButton = findViewById(R.id.givePermission);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveMeCamera();
            }
        });

    }

    private void giveMeCamera() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ActivityCompat.requestPermissions(InfoPermission2.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.ACCESS_WIFI_STATE},54);

        } else {
            Toast.makeText(InfoPermission2.this,
                    "You are under Lollipop you have permissions at install time",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case 54 :
                if (grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean wifiAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean wifiAccess = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted)
                    {
                        Intent intent = new Intent(InfoPermission2.this,BarcodeActivity.class);
                        startActivity(intent);
                        finish();

                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CHANGE_WIFI_STATE))
                            {
                                Toast.makeText(InfoPermission2.this,"You need to allow both of the permissions",Toast.LENGTH_SHORT).show();

                                requestPermissions(new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.CHANGE_WIFI_STATE
                                },54);

                            }

                        }
                    }

                }
                break;
        }
    }
}

