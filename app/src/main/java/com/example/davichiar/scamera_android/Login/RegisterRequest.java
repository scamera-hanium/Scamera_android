package com.example.davichiar.scamera_android.Login;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    final static private String URL = "http://15.164.192.198/UserRegister.php";
    private Map<String, String> parameters;

    public RegisterRequest(String userID, String userPassword, String userEmail, String userCheck, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();

        parameters.put("userID", userID);
        parameters.put("userPassword", userPassword);
        parameters.put("userEmail", userEmail);
        parameters.put("userCheck", userCheck);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}