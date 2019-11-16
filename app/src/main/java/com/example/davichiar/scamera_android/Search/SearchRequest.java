package com.example.davichiar.scamera_android.Search;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class SearchRequest extends StringRequest {
    final static private String URL = "http://15.164.192.198/SearchRegister.php";
    private Map<String, String> parameters;

    public SearchRequest(Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
    }
}
