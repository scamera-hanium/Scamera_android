package com.example.davichiar.scamera_android.Search;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davichiar.scamera_android.ImageSearch.ImageSearchActivity;
import com.example.davichiar.scamera_android.Main.MainActivity;
import com.example.davichiar.scamera_android.NaverImagePrint.GalleryAdapter;
import com.example.davichiar.scamera_android.NaverImagePrint.NaverImage;
import com.example.davichiar.scamera_android.NaverImagePrint.NaverImageItem;
import com.example.davichiar.scamera_android.NaverImagePrint.RestAPI;
import com.example.davichiar.scamera_android.NaverImagePrint.RestListenner;
import com.example.davichiar.scamera_android.R;
import com.google.android.gms.vision.text.Line;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.chrono.HijrahChronology;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ListView noticeListView;
    private NoticeListAdapter adapter;
    private List<Notice> noticedList;
    private AsyncTask<Void, Void, String> mTask=null;
    private Handler mHandler;
    private int timeTask = 0;
    private float addtest1 = 0, addtest2 = 0, acttest1 = 0, acttest2 = 0, sumtest = 0;
    private String nametest = "미판정";
    private int button1_count = 0, button2_count = 0;

    RecyclerView galleryView;
    ProgressBar processBar;
    ArrayList<NaverImageItem> imageItems;
    GalleryAdapter galleryAdapter;
    String pText, name;
    long lastClickTime = 0;
    long totalImages = 0;
    int final_check = 0, delaytime = 1000;
    CircleChart circleChart;

    PendingIntent intent, pendingIntent;
    NotificationCompat.Builder builder;
    NotificationManager notifManager;

    private List<ChartData> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Button button1 = (Button) findViewById(R.id.button1);
        final TextView button1_1 = (TextView) findViewById(R.id.button1_1);
        final TextView button1_2 = (TextView) findViewById(R.id.button1_2);
        final TextView button1_3 = (TextView) findViewById(R.id.button1_3);

        final Button button2 = (Button) findViewById(R.id.button2);
        final TextView button2_1 = (TextView) findViewById(R.id.button2_1);
        final TextView button2_2 = (TextView) findViewById(R.id.button2_2);
        final TextView button2_3 = (TextView) findViewById(R.id.button2_3);

        final Button button3 = (Button) findViewById(R.id.button3);
        final TextView button3_1 = (TextView) findViewById(R.id.button3_1);
        final TextView button3_2 = (TextView) findViewById(R.id.button3_2);
        final TextView button3_3 = (TextView) findViewById(R.id.button3_3);

        // circleChart = (CircleChart)findViewById(R.id.chart1);

        // ---------------------------------------------------------------------------------------ㄴ
        // 알림 서비스 사전 준비 소스
        getNotification();

        noticeListView = (ListView)findViewById(R.id.noticeListView);
        noticedList = new ArrayList<Notice>();
        noticedList.clear();

        adapter = new NoticeListAdapter(getApplicationContext(), noticedList);
        noticeListView.setAdapter(adapter);

        final EditText writeTextView = (EditText) findViewById(R.id.writeTextView);
        final TextView readTextView = (TextView) findViewById(R.id.readTextView);
        Intent intent = getIntent();
        name = intent.getExtras().getString("name"); /*String형*/
        writeTextView.setText(name);
        writeTextView.clearFocus();

        noticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = noticedList.get(position).getSearchLink();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                // Toast.makeText(getApplicationContext(), noticedList.get(position).getSearchLink(), Toast.LENGTH_SHORT).show();
            }
        });

        mHandler = new Handler() {
            public void handleMessage(Message msg) {

                if(mTask == null) {
                    mTask = new BackgroundTask();
                    mTask.execute();
                    // Toast.makeText(getApplicationContext(), mTask.getStatus().toString(), Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(100);
                }
                else if(final_check == 1) { }

                else if(mTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mHandler.sendEmptyMessageDelayed(100, delaytime);
                }

                else if(mTask.getStatus() == AsyncTask.Status.FINISHED) {
                    // Toast.makeText(getApplicationContext(), mTask.getStatus().toString(), Toast.LENGTH_SHORT).show();
                    readTextView.setText("블로그 리뷰 검색 수 (최근 100개 내역) : " + Math.round(sumtest) + "개");

                    if(addtest1 > 0 && addtest2 > 0) {
                        button1_1.setText("청정게시물 : " + Math.round(addtest1) + "개");
                        button1_2.setText("광고게시물 : " + Math.round(addtest2) + "개");
                        button1_3.setText("광고퍼센트 : " + Math.round(addtest2 / sumtest * 100) + "%");

                        if (Math.round(addtest2 / sumtest * 100) > 70) {
                            button1.setBackgroundColor(getResources().getColor(R.color.colorP3));
                        }
                        else if (Math.round(addtest2 / sumtest * 100) > 40) {
                            button1.setBackgroundColor(getResources().getColor(R.color.colorP2));
                        }
                        else {
                            button1.setBackgroundColor(getResources().getColor(R.color.colorP1));
                        }

                        // initData(Math.round(addtest1 / sumtest * 100), Math.round(addtest2 / sumtest * 100));
                    }
                    else {
                        button1_1.setText("-");
                        button1_2.setText("-");
                        button1_3.setText("-");
                    }

                    if(acttest1 > 0 || acttest2 > 0) {
                        button2_1.setText("긍정게시물 : " + Math.round(acttest1) + "개");
                        button2_2.setText("부정게시물 : " + Math.round(acttest2) + "개");
                        button2_3.setText("종합퍼센트 : " + Math.round(acttest1 / addtest1 * 100) + "%");

                        if (Math.round(acttest1 / addtest1 * 100) > 70) {
                            button2.setBackgroundColor(getResources().getColor(R.color.colorP1));
                        }
                        else if (Math.round(acttest1 / addtest1 * 100) > 40) {
                            button2.setBackgroundColor(getResources().getColor(R.color.colorP2));
                        }
                        else {
                            button2.setBackgroundColor(getResources().getColor(R.color.colorP3));
                        }
                    }
                    else {
                        button2_1.setText("-");
                        button2_2.setText("-");
                        button2_3.setText("-");
                    }

                    if(!nametest.equals("미판정")) {
                        String temp[] = nametest.split("'");

                        button3_1.setText("" + temp[1] + "개");
                        button3_2.setText("" + temp[3] + "개");
                        button3_3.setText("" + temp[5] + "개");
                        button3.setBackgroundColor(getResources().getColor(R.color.colorP1));

                        if(final_check == 0) {
                            builder.setContentTitle("SCamera") // required
                                    .setContentText("상품 분석이 완료되었습니다.")  // required
                                    .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                                    .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                                    .setSound(RingtoneManager
                                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setSmallIcon(R.drawable.logo_title)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources()
                                            , R.drawable.logo))
                                    .setBadgeIconType(R.drawable.logo)
                                    .setContentIntent(pendingIntent);
                            notifManager.notify(0, builder.build());

                            final_check = 1;
                        }
                    }
                    else {
                        button3_1.setText("-");
                        button3_2.setText("-");
                        button3_3.setText("-");
                    }
                    mTask = new BackgroundTask();
                    mTask.execute();

                    mHandler.sendEmptyMessage(100);
                }
            }
        };
        mHandler.sendEmptyMessage(100);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button1_count == 0) {
                    if(final_check == 1 && addtest1 > 0) {
                        Toast.makeText(getApplicationContext(), "필터링 : 청정게시물", Toast.LENGTH_SHORT).show();
                        button1_count = 1;
                        mTask = new BackgroundTask();
                        mTask.execute();
                    }
                    else if(final_check == 1 && addtest1 == 0) {
                        Toast.makeText(getApplicationContext(), "청정 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                        button1_count = 1;
                    }
                }
                else {
                    if(final_check == 1) {
                        Toast.makeText(getApplicationContext(), "필터링 해제", Toast.LENGTH_SHORT).show();
                        button1_count = 0;
                        mTask = new BackgroundTask();
                        mTask.execute();
                    }
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button2_count == 0) {
                    if(final_check == 1 && acttest1 > 0) {
                        Toast.makeText(getApplicationContext(), "필터링 : 긍정게시물", Toast.LENGTH_SHORT).show();
                        button2_count = 1;
                        mTask = new BackgroundTask();
                        mTask.execute();
                    }
                    else if(final_check == 1 && acttest1 == 0) {
                        Toast.makeText(getApplicationContext(), "긍정 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                        button2_count = 1;
                    }
                }
                else if(button2_count == 1) {
                    if(final_check == 1 && acttest2 > 0) {
                        Toast.makeText(getApplicationContext(), "필터링 : 부정게시물", Toast.LENGTH_SHORT).show();
                        button2_count = 2;
                        mTask = new BackgroundTask();
                        mTask.execute();
                    }
                    else if(final_check == 1 && acttest2 == 0) {
                        Toast.makeText(getApplicationContext(), "부정 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                        button2_count = 2;
                    }
                }
                else {
                    if(final_check == 1) {
                        Toast.makeText(getApplicationContext(), "필터링 해제", Toast.LENGTH_SHORT).show();
                        button2_count = 0;
                        mTask = new BackgroundTask();
                        mTask.execute();
                    }
                }
            }
        });

        viewPreference();
        initRecycler();

        searchImageByQueryText(false);
        detectScrollToLast();

        RelativeLayout imageViewLayout = (RelativeLayout) findViewById(R.id.imageViewLayout);
        LinearLayout noticeViewLayout = (LinearLayout) findViewById(R.id.notice);
        RelativeLayout fragmentViewLayout = (RelativeLayout) findViewById(R.id.fragment);
    }

    private void initData(int Data1, int Data2){
        datas.clear();
        ChartData data1 = new ChartData();
        data1.setText("긍정");
        data1.setTextColor(Color.parseColor("#EED66D"));
        data1.setPercentage(Data1);
        data1.setBackgroundStrokeColor(Color.parseColor("#FFFBF1"));
        data1.setBackgroundColor(Color.parseColor("#FFFFFC"));
        data1.setColor(Color.parseColor("#FBFDBA"));
        data1.setStrokeColor(Color.parseColor("#EED66D"));
        ChartData data2 = new ChartData();
        data2.setText("부정");
        data2.setTextColor(Color.parseColor("#74EAD9"));
        data2.setPercentage(Data2);
        data2.setBackgroundStrokeColor(Color.parseColor("#ECFFFD"));
        data2.setBackgroundColor(Color.parseColor("#F9FEFF"));
        data2.setColor(Color.parseColor("#9EFBEF"));
        data2.setStrokeColor(Color.parseColor("#74EAD9"));
        datas.add(data1);
        datas.add(data2);

        circleChart.openLog();
        circleChart.setTextSlope();
        circleChart.setData(datas);
        circleChart.run();
    }

    public void getNotification() {
        // 알림 서비스 관련 소스
        String channelId = "channel";
        String channelName = "Channel Name";
        notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext()
                , SearchActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();

        pendingIntent
                = PendingIntent.getActivity(getApplicationContext()
                , requestID
                , notificationIntent
                , PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void viewPreference() {
        galleryView = (RecyclerView) findViewById(R.id.galleryView);
        processBar = (ProgressBar) findViewById(R.id.processBar);
    }

    private void initRecycler() {
        imageItems = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(imageItems, this);
        galleryView.setAdapter(galleryAdapter);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryView.setHasFixedSize(false);
    }

    private void detectScrollToLast() {
        final GridLayoutManager gridLayoutManager = (GridLayoutManager) galleryView
                .getLayoutManager();

        galleryView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItem = gridLayoutManager
                        .findLastVisibleItemPosition();
                if (totalImages > 100 && totalImages != imageItems.size()
                        && totalItemCount <= (lastVisibleItem + 3)) {
                    processBar.setVisibility(View.VISIBLE);
                    searchImageByQueryText(true);
                }
            }
        });
    }

    private void searchImageByQueryText(final boolean isMore) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        int start = 0;
        if (!isMore) {
            pText = name;
            imageItems.clear();
            totalImages = 0;
            galleryAdapter.notifyDataSetChanged();
        } else {
            start = imageItems.size();
        }
        if (!TextUtils.isEmpty(pText)) {
            RestAPI.GetAPI(this, RestAPI.getQueryImageUrl(pText, start), new RestListenner() {
                @Override
                public void onSuccess(NaverImage naverImage) {
                    processBar.setVisibility(View.GONE);
                    totalImages = naverImage.getTotal();
                    imageItems.addAll(naverImage.getItems());
                    galleryAdapter.notifyItemInserted(imageItems.size());

                }
                @Override
                protected void onError(int httpCode, String error) {
                    super.onError(httpCode, error);
                    processBar.setVisibility(View.GONE);
                }
            });
        }
    }

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected void onPreExecute() {
            target = "http://15.164.192.198/NoticeList.php";
            noticedList.clear();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;//결과 값을 여기에 저장함
                StringBuilder stringBuilder = new StringBuilder();

                while((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                String searchTitle, searchLink, searchImglink, searchContext, searchDate, searchNicname, searchAdd, searchActive, searchText;

                timeTask = jsonArray.length();
                int count = 0;
                int addcount1 = 0, addcount2 = 0, actcount1 = 0, actcount2 = 0;
                while(count < jsonArray.length()) {
                    JSONObject object = jsonArray.getJSONObject(count);
                    searchTitle = object.getString("TITLE");
                    searchLink = object.getString("LINK");
                    searchImglink = object.getString("IMGLINK");
                    searchContext = object.getString("CONTEXT1");
                    searchDate = object.getString("DATE");
                    searchNicname = object.getString("NICNAME");
                    searchAdd = object.getString("ADD_TEXT");
                    searchActive = object.getString("ACTIVE_TEXT");
                    searchText = object.getString("TEXT");

                    if(searchAdd.equals("청정"))
                        addcount1++;
                    if(searchAdd.equals("사진 광고") || searchAdd.equals("업체 광고") || searchAdd.equals("닉네임 광고") || searchAdd.equals("글 광고"))
                        addcount2++;

                    if(searchActive.equals("긍정"))
                        actcount1++;
                    if(searchActive.equals("부정"))
                        actcount2++;

                    if(button1_count == 0 && button2_count == 0) {
                        Notice notice = new Notice(searchTitle, searchLink, searchImglink, searchContext, searchDate, searchNicname, searchAdd, searchActive, searchText);
                        noticedList.add(notice);
                    }
                    else if(searchAdd.equals("청정") && button2_count == 0) {
                        Notice notice = new Notice(searchTitle, searchLink, searchImglink, searchContext, searchDate, searchNicname, searchAdd, searchActive, searchText);
                        noticedList.add(notice);
                    }
                    else if(searchAdd.equals("청정") && searchActive.equals("긍정") && button2_count == 1) {
                        Notice notice = new Notice(searchTitle, searchLink, searchImglink, searchContext, searchDate, searchNicname, searchAdd, searchActive, searchText);
                        noticedList.add(notice);
                    }
                    else if(searchAdd.equals("청정") && searchActive.equals("부정") && button2_count == 2) {
                        Notice notice = new Notice(searchTitle, searchLink, searchImglink, searchContext, searchDate, searchNicname, searchAdd, searchActive, searchText);
                        noticedList.add(notice);
                    }
                    count++;
                }
                nametest = noticedList.get(0).getSearchText();
                addtest1 = addcount1;
                addtest2 = addcount2;
                acttest1 = actcount1;
                acttest2 = actcount2;
                sumtest = noticedList.size();

                adapter.notifyDataSetChanged();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //9강때 추가된부분 백버튼을 두번 누르면 앱이 종료되게 함.
    private long pressedTime;

    @Override
    public void onBackPressed() {
        if(final_check == 0) {
            Toast.makeText(SearchActivity.this, "창 모드로 전환됩니다.", Toast.LENGTH_LONG).show();
            moveTaskToBack(true);
        }
        else {
            //백버튼이 눌리고 1.5초안에 또 눌리면 종료가됨
            if (pressedTime == 0) {
                Toast.makeText(SearchActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
                pressedTime = System.currentTimeMillis();
            }

            else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(SearchActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
                    pressedTime = 0;
                }
                else {
                    finishAffinity();
                    System.runFinalization();
                    System.exit(0);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        // mHandler.sendEmptyMessage(100);
        // final_check = 0;
        // Toast.makeText(getApplicationContext(), "onResume", Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    @Override
    protected void onStop() {
        // mHandler.removeMessages(100);
        // mTask.cancel(true);
        // Toast.makeText(getApplicationContext(), "onStop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }
}