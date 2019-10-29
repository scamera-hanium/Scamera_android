package com.example.davichiar.scamera_android.Login;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class LoginCheck extends StringRequest {

    final static private String URL = "http://15.164.192.198/LoginCheck.php";
    private Map<String, String> parameters;

    public LoginCheck(Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
    }
}
