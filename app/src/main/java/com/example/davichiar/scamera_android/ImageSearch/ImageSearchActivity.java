package com.example.davichiar.scamera_android.ImageSearch;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davichiar.scamera_android.BuildConfig;
import com.example.davichiar.scamera_android.Main.MainActivity;
import com.example.davichiar.scamera_android.TextSearch.NaverTranslate;
import com.example.davichiar.scamera_android.TextSearch.TextSearchActivity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.api.services.vision.v1.model.DominantColorsAnnotation;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.davichiar.scamera_android.R;

public class ImageSearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
        private static final int RECORD_REQUEST_CODE = 101;
        private static final int CAMERA_REQUEST_CODE = 102;

        private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;

        @BindView(R.id.takePicture)
        ImageView takePicture;

        @BindView(R.id.imageView)
        ImageView imageView;

        ImageView TranslateButton, completeButton;
        int check = 0;
        private static String mTestString, clipBoardText = null;;

        @BindView(R.id.visionAPIData)
        TextView visionAPIData;
        private Feature feature;
        private Bitmap bitmap;
        private String[] visionAPI = new String[]{"LABEL_DETECTION"};

        private String api = visionAPI[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        ButterKnife.bind(this);

        feature = new Feature();
        feature.setType(visionAPI[0]);
        feature.setMaxResults(10);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureFromCamera();
            }
        });

        Toast.makeText(getApplicationContext(), "분석할 사진을 찍어주세요", Toast.LENGTH_SHORT).show();
        takePictureFromCamera();

        TranslateButton = findViewById(R.id.translateButton);

        TranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check == 0) {
                    final NaverTranslate naverAPI = new NaverTranslate();
                    new Thread(){
                        public void run() {
                            try {
                                final String result = naverAPI.translate(mTestString);
                                runOnUiThread(new Runnable() {
                                    // 된다
                                    @Override
                                    public void run() {
                                        visionAPIData.setText(result);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    TranslateButton.setImageDrawable(getResources().getDrawable(R.drawable.timage4));
                    check = 1;
                }
                else {
                    visionAPIData.setText(mTestString);
                    TranslateButton.setImageDrawable(getResources().getDrawable(R.drawable.timage2));

                    check = 0;
                }
            }
        });

        completeButton = findViewById(R.id.completeButton);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(clipBoardText == null) {
                    Toast.makeText(getApplicationContext(), "텍스트를 다시 복사해주시고, 종료를 원하시면 이전 버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent1 = new Intent(ImageSearchActivity.this, MainActivity.class);
                    intent1.putExtra("1", clipBoardText);
                    startActivityForResult(intent1,1);
                }
            }
        });

        ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
                ClipData clipData = clipBoard.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                clipBoardText = item.getText().toString();
                Toast.makeText(getApplicationContext(), "복사된 텍스트 : " + clipBoardText, Toast.LENGTH_SHORT).show();

                // Access your context here using YourActivityName.this
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture.setVisibility(View.VISIBLE);
        } else {
            takePicture.setVisibility(View.INVISIBLE);
            makeRequest(Manifest.permission.CAMERA);
        }
    }

    private int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void makeRequest(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, RECORD_REQUEST_CODE);
    }

    public void takePictureFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            callCloudVision(bitmap, feature);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            } else {
                takePicture.setVisibility(View.VISIBLE);
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap, final Feature feature) {
        final List<Feature> featureList = new ArrayList<>();
        featureList.add(feature);

        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));
        annotateImageRequests.add(annotateImageReq);


        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                } catch (IOException e) {
                }
                return "분석에 실패하였습니다. 사진을 다시 찍어주세요.";
            }

            protected void onPostExecute(String result) {
                visionAPIData.setText(result);
                mTestString = result;
            }
        }.execute();
    }

    @NonNull
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;

        String message = "";
        entityAnnotations = imageResponses.getLabelAnnotations();
        message = formatAnnotation(entityAnnotations);

        return message;
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";

        if (entityAnnotation != null) {
            for (EntityAnnotation entity : entityAnnotation) {
                String strNumber = String.format("%.2f", entity.getScore());

                message = message + "    " + entity.getDescription() + " [정확도 : " + strNumber + "]";
                message += "\n";
            }
        } else {
            message = "Nothing Found";
        }
        return message;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        api = (String) adapterView.getItemAtPosition(i);
        feature.setType(api);
        if (bitmap != null)
            callCloudVision(bitmap, feature);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "복사 작업이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(ImageSearchActivity.this, MainActivity.class);
        startActivity(intent1);
    }
}

