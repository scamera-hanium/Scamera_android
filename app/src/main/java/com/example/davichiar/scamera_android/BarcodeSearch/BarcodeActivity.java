package com.example.davichiar.scamera_android.BarcodeSearch;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import ezvcard.Ezvcard;
import ezvcard.VCard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davichiar.scamera_android.Main.MainActivity;
import com.example.davichiar.scamera_android.QRSearch.QRCodeActivity;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.davichiar.scamera_android.R;

public class BarcodeActivity extends AppCompatActivity {

    private static final int DATARESULT = 7346;
    String barcodeResult, clipBoardText = null;
    int checkResult = 0, clipboardcheck = 0;
    String stringvalue = "null";
    ImageView scanButton, completeButton;

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        clipboardcheck = 0;
        scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(BarcodeActivity.this, BarcodeActivity.class);
                startActivity(intent1);
            }
        });
        completeButton = findViewById(R.id.completeButton);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (clipBoardText == null || clipboardcheck <= 1) {
                    Toast.makeText(getApplicationContext(), "WEB 내에서 텍스트를 복사 한 후, COMPLETE 버튼을 누르세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(BarcodeActivity.this, MainActivity.class);
                    intent1.putExtra("1", clipBoardText);
                    startActivityForResult(intent1, 1);
                }
            }
        });

        Intent intent = new Intent(this, CameraPreview2.class);
        startActivityForResult(intent, DATARESULT);

        mWebView = (WebView) findViewById(R.id.webView);

        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
                ClipData clipData = clipBoard.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                clipBoardText = item.getText().toString();
                Toast.makeText(getApplicationContext(), "복사된 텍스트 : " + clipBoardText, Toast.LENGTH_SHORT).show();

                clipboardcheck += 1;
                // Access your context here using YourActivityName.this
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (DATARESULT == requestCode) {
            if (data != null) {
                Barcode barcode = data.getParcelableExtra("Barcode");
                stringvalue = data.getStringExtra("Raw Value");
                barcodeResult = stringvalue;
                dataOperations(stringvalue, barcode);

                if (checkResult == 0) {
                    Toast.makeText(getApplicationContext(), "잘못된 형식의 바코드 입니다.", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(BarcodeActivity.this, BarcodeActivity.class);
                    startActivity(intent1);
                } else {
                    ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Test Data", barcodeResult);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(getApplicationContext(), "복사된 바코드 : " + barcodeResult + "\n" + "WEB 내에서 텍스트를 복사 한 후, COMPLETE 버튼을 누르세요.", Toast.LENGTH_SHORT).show();

                    mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
                    mWebSettings = mWebView.getSettings(); //세부 세팅 등록
                    mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
                    mWebSettings.setSupportMultipleWindows(true); // 새창 띄우기 허용 여부
                    mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
                    mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
                    mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
                    mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
                    mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
                    mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
                    mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
                    mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
                    mWebView.loadUrl("http://www.gs1kr.org/Service/Member/appl/member03.asp"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
                }
            } else {
                Toast.makeText(getApplicationContext(), "스캔이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(BarcodeActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        }
    }

    private void dataOperations(String stringvalue, Barcode barcode) {

        Pattern p = Pattern.compile("(^[0-9]*$)");

        Matcher m = p.matcher(stringvalue);

        if(m.find()) checkResult = 1;
        else checkResult = 0;
    }

    private String getParam(String s) {
        return s.split(":")[1];
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "복사 작업이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(BarcodeActivity.this, MainActivity.class);
        startActivity(intent1);
    }
}
