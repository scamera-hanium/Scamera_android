package com.example.davichiar.scamera_android.Main;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.view.View;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.davichiar.scamera_android.BarcodeSearch.BarcodeActivity;
import com.example.davichiar.scamera_android.BarcodeSearch.InfoPermission2;
import com.example.davichiar.scamera_android.ImageSearch.ImageSearchActivity;
import com.example.davichiar.scamera_android.QRSearch.QRCodeActivity;
import com.example.davichiar.scamera_android.TextSearch.TextSearchActivity;
import com.example.davichiar.scamera_android.Login.LoginActivity;
import com.example.davichiar.scamera_android.Login.LoginCheck;
import com.example.davichiar.scamera_android.Login.MainRequest;
import com.example.davichiar.scamera_android.Login.RegisterCheck;
import com.example.davichiar.scamera_android.R;
import com.example.davichiar.scamera_android.Search.SearchActivity;
import com.example.davichiar.scamera_android.QRSearch.InfoPermission;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;

    private AlertDialog dialog;
    private String searchText;
    private TextView writeText;
    private ImageButton loginButton;

    private String jsonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ---------------------------------------------------------------------------------------
        // 2. 로그인 관련 소스
        writeText = (TextView) findViewById(R.id.writeText);
        writeText.setVisibility(View.GONE);

        writeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                dialog = builder.setMessage("로그아웃 하시겠습니까?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Response.Listener<String> checkListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        writeText.setVisibility(View.GONE);
                                        loginButton.setVisibility(View.VISIBLE);
                                    }
                                };

                                RegisterCheck registerCheck = new RegisterCheck(jsonText, String.valueOf(0), checkListener);
                                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                                queue.add(registerCheck);
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        // ---------------------------------------------------------------------------------------
        // 3. 홈 버튼 관련 서비스 (알림 관련)
        final EditText searchEditText = (EditText) findViewById(R.id.searchEditText);
        ImageView homeButton = (ImageView) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });

        // ---------------------------------------------------------------------------------------
        // 4. 로그인 버튼 관련 서비스
        loginButton = (ImageButton) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(loginIntent);
            }
        });

        // ---------------------------------------------------------------------------------------
        // 5. 검색 관련 버튼 서비스
        ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = searchEditText.getText().toString();
                searchEditText.setText("");

                if(searchText.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    dialog = builder.setMessage("빈 검색어는 입력이 불가능합니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success) {
                                // Toast.makeText(getApplicationContext(), searchText, Toast.LENGTH_SHORT).show();
                                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                                searchIntent.putExtra("name",searchText); /*송신*/
                                MainActivity.this.startActivity(searchIntent);
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                dialog = builder.setMessage("검색을 다시 시도해주세요.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                MainRequest mainRequest = new MainRequest(searchText, String.valueOf(0), responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(mainRequest);
            }
        });

        // ---------------------------------------------------------------------------------------
        // 6. QRCode 관련 서비스
        LinearLayout qrButton = (LinearLayout) findViewById(R.id.qrbutton);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this,InfoPermission.class);
                    startActivity(intent);
                    finish();

                }else{
                    Intent intent = new Intent(MainActivity.this,QRCodeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        Intent intent = getIntent();
        String inputData = intent.getStringExtra("1");
        if(inputData != null) {
            searchEditText.setText(inputData);
        }
        ClipboardManager clipboard;
        clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipboard.removePrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                ClipData cd = clipboard.getPrimaryClip();
            }
        });;

        // ---------------------------------------------------------------------------------------
        // 7. BarCode 관련 서비스
        LinearLayout barcodeButton = (LinearLayout) findViewById(R.id.barcodebutton);
        barcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this,InfoPermission2.class);
                    startActivity(intent);
                    finish();

                }else{
                    Intent intent = new Intent(MainActivity.this,BarcodeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // ---------------------------------------------------------------------------------------
        // 8. OCR Scan 관련 서비스
        LinearLayout ocrButton = (LinearLayout) findViewById(R.id.ocrbutton);
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ocrIntent = new Intent(MainActivity.this, TextSearchActivity.class);
                MainActivity.this.startActivity(ocrIntent);
            }
        });


        // ---------------------------------------------------------------------------------------
        // 9. Image Button 관련 서비스
        LinearLayout imageButton = (LinearLayout) findViewById(R.id.imagebutton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(MainActivity.this, ImageSearchActivity.class);
                MainActivity.this.startActivity(imageIntent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Response.Listener<String> checkListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    jsonText = jsonResponse.getString("userID");
                    boolean success = jsonResponse.getBoolean("success");
                    if(success) {
                        writeText.setVisibility(View.VISIBLE);
                        writeText.setText(jsonText + "님 안녕하세요!");
                        loginButton.setVisibility(View.GONE);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        LoginCheck loginCheck = new LoginCheck(checkListener);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(loginCheck);
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
