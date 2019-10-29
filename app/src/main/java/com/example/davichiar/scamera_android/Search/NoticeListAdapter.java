package com.example.davichiar.scamera_android.Search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davichiar.scamera_android.R;
import com.example.davichiar.scamera_android.Search.Notice;

import java.util.List;
import java.util.regex.Pattern;

public class NoticeListAdapter extends BaseAdapter {

    private Context context;
    private List<Notice> noticeList;
    private Pattern pattern;

    public NoticeListAdapter(Context context, List<Notice> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @Override
    public int getCount() {
        return noticeList.size();
    }

    @Override
    public Object getItem(int position) {
        return noticeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.notice, null);
        LinearLayout mainLayout = (LinearLayout) v.findViewById(R.id.mainLayout);

        ImageView mainImage = (ImageView) v.findViewById(R.id.mainImage);
        TextView mainTitle = (TextView) v.findViewById(R.id.mainTitle);
        TextView mainContent = (TextView) v.findViewById(R.id.mainContent);
        TextView mainURL = (TextView) v.findViewById(R.id.mainURL);

        try {
            if(noticeList.get(position).getSearchAdd().equals("미판정") || noticeList.get(position).getSearchActive().equals("미판정")){
                mainImage.setImageDrawable(v.getResources().getDrawable(R.drawable.loading));
                mainLayout.setBackground(v.getResources().getDrawable(R.drawable.rect_gray));
            }
            else if(noticeList.get(position).getSearchAdd().equals("청정")) {
                if(noticeList.get(position).getSearchActive().equals("긍정")) {
                    mainImage.setImageDrawable(v.getResources().getDrawable(R.drawable.happy));
                    mainLayout.setBackground(v.getResources().getDrawable(R.drawable.rect_green));
                }
                else {
                    mainImage.setImageDrawable(v.getResources().getDrawable(R.drawable.sad));
                    mainLayout.setBackground(v.getResources().getDrawable(R.drawable.rect_gray));
                }
            }
            else {
                mainImage.setImageDrawable(v.getResources().getDrawable(R.drawable.advertisement));
                mainLayout.setBackground(v.getResources().getDrawable(R.drawable.rect_red));
            }

            mainTitle.setText(noticeList.get(position).getSearchTitle());
            mainURL.setText(noticeList.get(position).getSearchLink());
            mainContent.setText(noticeList.get(position).getSearchContext() + "\n\n날짜 : " +
                    noticeList.get(position).getSearchDate() + "\n닉네임 : " + noticeList.get(position).getSearchNicname());
        }
        catch(Exception e) {}

        return v;
    }
}