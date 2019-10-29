package com.example.davichiar.scamera_android.NaverImagePrint;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.davichiar.scamera_android.R;

public class NaverSearchActivity extends AppCompatActivity implements OnClickListener, TextView.OnEditorActionListener {

    RecyclerView galleryView;
    EditText edText;
    Button btnSearch;
    ProgressBar processBar;
    ArrayList<NaverImageItem> imageItems;
    GalleryAdapter galleryAdapter;
    String pText;
    long lastClickTime = 0;
    long totalImages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver_search);
        viewPreference();
        initRecycler();
        detectScrollToLast();
    }

    private void viewPreference() {
        galleryView = (RecyclerView) findViewById(R.id.galleryView);
        edText = (EditText) findViewById(R.id.edText);
        edText.setOnEditorActionListener(this);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        processBar = (ProgressBar) findViewById(R.id.processBar);
    }

    private void initRecycler() {
        imageItems = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(imageItems, this);
        galleryView.setAdapter(galleryAdapter);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryView.setHasFixedSize(false);
    }

    @Override
    public void onClick(View view) {
        searchImageByQueryText(false);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEARCH) {
            searchImageByQueryText(false);
            return true;
        }
        return false;
    }

    private void searchImageByQueryText(final boolean isMore) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        hideKeyBoard();
        int start = 0;
        if (!isMore) {
            pText = edText.getText().toString().trim();
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

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edText.getWindowToken(), 0);
    }
}
