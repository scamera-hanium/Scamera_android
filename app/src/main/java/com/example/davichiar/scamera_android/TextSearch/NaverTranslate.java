package com.example.davichiar.scamera_android.TextSearch;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaverTranslate  {

    public String translate(String soricreMessage) throws  Exception {

        String text = URLEncoder.encode(soricreMessage, "UTF-8");
        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("X-Naver-Client-Id", clientId);
        con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

        String postParams = "source=en&target=ko&text=" + text; // 번열될 부분
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        BufferedReader br;  // 리퀘스트 쏘고 받고 하는 과정들
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }
        else {  // 에러 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        /*json 타입으로 돌려줬다   상호호환이 안되서 json으로 돌려줬다*/
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();
        JSONObject result = new JSONObject(response.toString());
        String resultMesages =  result.getJSONObject("message").
                getJSONObject("result").getString("translatedText");
        return  resultMesages;
    }
}

