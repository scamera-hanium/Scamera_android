package com.example.davichiar.scamera_android.NaverImagePrint;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.net.URLEncoder;

public class RestAPI {

    private static final String ROOT_URL = "https://openapi.naver.com/v1/search/image?query=%s&display=100&filter=all&start=%d";

    public static String getQueryImageUrl(String s, int start) {
        String text = s;
        try {
            text = URLEncoder.encode(s, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (start == 0) {
            start = 1;
        }
        return String.format(ROOT_URL, text, start);
    }

    public static void GetAPI(final Context context, String url, final RestListenner restListenner) {
        Log.i("LTH", url);
        Ion.with(context)
                .load(url)
                .addHeader("X-Naver-Client-Id", "wYRlTYlyvgQCJJ3lxZzo")
                .addHeader("X-Naver-Client-Secret", "B0cwT7BY6j")
                .as(NaverImage.class)
                .withResponse()
                .setCallback(new FutureCallback<Response<NaverImage>>() {
                    @Override
                    public void onCompleted(Exception e, Response<NaverImage> result) {
                        if (result != null) {
                            if (result.getHeaders().code() == 200 || result.getHeaders().code() == 201) {
                                if (result.getResult().getItems().size() > 0) {
                                    restListenner.onSuccess(result.getResult());
                                } else {
                                    restListenner.onError(0, "List image is empty");
                                }
                            } else {
                                restListenner.onError(result.getHeaders().code(), "");
                            }
                        } else {
                            restListenner.onError(0, "Network offline");
                        }
                    }
                });
    }
}