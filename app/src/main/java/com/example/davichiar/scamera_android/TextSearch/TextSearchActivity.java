package com.example.davichiar.scamera_android.TextSearch;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davichiar.scamera_android.BarcodeSearch.BarcodeActivity;
import com.example.davichiar.scamera_android.BuildConfig;
import com.example.davichiar.scamera_android.Main.MainActivity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Page;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Word;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.davichiar.scamera_android.R;

public class TextSearchActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = TextSearchActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private ImageView mMainImage, TranslateButton, completeButton;
    private static String mTestString, clipBoardText = null;;

    private int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_search);

        AlertDialog.Builder builder2 = new AlertDialog.Builder(TextSearchActivity.this);
        builder2
                .setMessage("사진 올리는 방법을 선택하세요.")
                .setPositiveButton("Album", (dialog, which) -> startGalleryChooser())
                .setNegativeButton("Camera", (dialog, which) -> startCamera());
        builder2.create().show();


        ImageView fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TextSearchActivity.this);
            builder
                    .setMessage("사진 올리는 방법을 선택하세요.")
                    .setPositiveButton("Album", (dialog, which) -> startGalleryChooser())
                    .setNegativeButton("Camera", (dialog, which) -> startCamera());
            builder.create().show();
        });

        mImageDetails = findViewById(R.id.image_details);
        mMainImage = findViewById(R.id.main_image);
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
                                        mImageDetails.setText(result);
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
                    mImageDetails.setText(mTestString);
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
                    Intent intent1 = new Intent(TextSearchActivity.this, MainActivity.class);
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

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap processImage(Bitmap orig_img)
    {
        return orig_img;
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                add(labelDetection);
            }});
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        annotateRequest.setDisableGZipContent(true);

        return annotateRequest;
    }

    private static class DocumentTextDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<TextSearchActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        DocumentTextDetectionTask(TextSearchActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
            } catch (IOException e) {
            }
            return "분석에 실패하였습니다. 사진을 다시 찍거나 골라주세요.";
        }

        protected void onPostExecute(String result) {
            TextSearchActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
                imageDetail.setText(result);

                mTestString = result;
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        mImageDetails.setText(R.string.loading_message);

        try {
            AsyncTask<Object, Void, String> documentTextDetectionTask = new DocumentTextDetectionTask(this, prepareAnnotationRequest(bitmap));
            documentTextDetectionTask.execute();
        } catch (IOException e) {
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("원하는 내용을 복사한 뒤, COMPLETE 버튼을 눌러주세요\n\n");

        TextAnnotation annotation = response.getResponses().get(0).getFullTextAnnotation();
        for (Page page : annotation.getPages())
        {
            String pageText = "";
            for (Block block : page.getBlocks())
            {
                String blockText = "";
                for (Paragraph para : block.getParagraphs())
                {
                    String paraText = "";
                    for(Word word: para.getWords())
                    {
                        String wordText = "";
                        for( Symbol symbol : word.getSymbols())
                        {
                            wordText = wordText + symbol.getText();
                        }
                        paraText = paraText + " " + wordText;
                    }
                    blockText = blockText + paraText;
                }
                pageText = pageText + blockText;
            }
        }

        message.append(annotation.getText());
        return message.toString();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "복사 작업이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(TextSearchActivity.this, MainActivity.class);
        startActivity(intent1);
    }
}
