package com.bignerdranch.android.moviegallery.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @see <a href="https://www.dev2qa.com/use-okhttp3-to-upload-and-download-json-file-example/">blog</a>
 */
public abstract class OkHttpUtil {
    private static final String TAG = "OkHttpUtil";

    private static final OkHttpClient okHttpClient;
    static {
        okHttpClient= new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,TimeUnit.SECONDS)
                .writeTimeout(5,TimeUnit.SECONDS)
                .build();

    }

    private static final int REQUEST_CODE_READ_EXTERNAL_PERMISSION = 1;


    public static void uploadAppFileByMethod(File file, String uploadUrl,Method method,Callback callback) {
        // Create upload file content mime type.
        MediaType fileContentType = MediaType.parse("File/*");

//            String uploadFileRealPath = "/sdcard/Movies/test.txt";
//            // Create file object.
//            File file = new File(uploadFileRealPath);

        // Create request body.
        RequestBody requestBody = RequestBody.create(fileContentType, file);


        // Create request builder.
        Request.Builder builder = new Request.Builder();
        // Set url.
        builder = builder.url(uploadUrl);
        // set method and request body.
        builder.method(method.getName(), requestBody);

        // Create request object.
        Request request = builder.build();
        // Get okhttp3.Call object.
        Call call = okHttpClient.newCall(request);

        // Execute the call asynchronously.
        call.enqueue(callback);

    }
    public static void uploadAppFileByPost(File file, String uploadUrl,Callback callback) {
        // Create upload file content mime type.
        MediaType fileContentType = MediaType.parse("File/*");

//            String uploadFileRealPath = "/sdcard/Movies/test.txt";
//            // Create file object.
//            File file = new File(uploadFileRealPath);

        // Create request body.
        RequestBody requestBody = RequestBody.create(fileContentType, file);

        // Send request body.
//            sendRequestBody("http://www.bing.com", requestBody);
        sendRequestBody(uploadUrl, requestBody,callback);

    }

    public void uploadExternalFileByPost(Activity activity, File file, String uploadUrl) {
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            String[] requirePermission = {Manifest.permission.READ_EXTERNAL_STORAGE};

            // Below code will invoke method onRequestPermissionsResult.
            ActivityCompat.requestPermissions(activity, requirePermission, REQUEST_CODE_READ_EXTERNAL_PERMISSION);
        } else {

            // Create upload file content mime type.
            MediaType fileContentType = MediaType.parse("File/*");

//            String uploadFileRealPath = "/sdcard/Movies/test.txt";
//            // Create file object.
//            File file = new File(uploadFileRealPath);

            // Create request body.
            RequestBody requestBody = RequestBody.create(fileContentType, file);

            // Send request body.
//            sendRequestBody("http://www.bing.com", requestBody);
            sendRequestBody(uploadUrl, requestBody);
        }
    }

    public static void sendJsonByPost(String jsonString) {
        // Json data mime type string value.
        String jsonMimeType = "application/json; charset=utf-8";

// Get json string media type.
        MediaType jsonContentType = MediaType.parse(jsonMimeType);

// Json string.
//        String jsonString = "{\"username\":\"jerry\",\"password\":\"1234568\"}";

// Create json string request body
        RequestBody jsonRequestBody = RequestBody.create(jsonContentType, jsonString);

        sendRequestBody("http://www.bing.com", jsonRequestBody);
    }

    public static void sendMultipart( ) {
        // Create upload file content mime type.
        MediaType fileContentType = MediaType.parse("File/*");

        String uploadFileRealPath = "/sdcard/Movies/test.txt";

// Create file object.
        File file = new File(uploadFileRealPath);

// Create request body.
        RequestBody requestBody = RequestBody.create(fileContentType, file);

// Create multipart body builder.
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();

// Set type.
        multipartBuilder.setType(MultipartBody.FORM);

// Add string parameters.
        multipartBuilder.addFormDataPart("name", "jerry");
        multipartBuilder.addFormDataPart("password", "12345678");

// Add file content.
        multipartBuilder.addPart(requestBody);

// Get multipartbody object.
        MultipartBody multipartBody = multipartBuilder.build();

        sendRequestBody("http://www.bing.com", multipartBody);
    }
    // Send http request with request body, the body can contain Json string, file or multiple part ( post params and file.)
    private static void sendRequestBody(String url, RequestBody requestBody,Callback callback) {
        // Create request builder.
        Request.Builder builder = new Request.Builder();
        // Set url.
        builder = builder.url(url);
        // Post request body.
        builder = builder.post(requestBody);
        // Create request object.
        Request request = builder.build();
        // Get okhttp3.Call object.
        Call call = okHttpClient.newCall(request);

        // Execute the call asynchronously.
        call.enqueue(callback);
    }


    // Send http request with request body, the body can contain Json string, file or multiple part ( post params and file.)
    private static void sendRequestBody(String url, RequestBody requestBody) {
        // Create request builder.
        Request.Builder builder = new Request.Builder();
        // Set url.
        builder = builder.url(url);
        // Post request body.
        builder = builder.post(requestBody);
        // Create request object.
        Request request = builder.build();
        // Get okhttp3.Call object.
        Call call = okHttpClient.newCall(request);

        // Execute the call asynchronously.
        call.enqueue(new Callback() {
            // If request fail.
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            // If request success.
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int respCode = response.code();
                String respMsg = response.message();
                String respBody = response.body().string();

                Log.d(TAG, "Response code : " + respCode);
                Log.d(TAG, "Response message : " + respMsg);
                Log.d(TAG, "Response body : " + respBody);
            }
        });


    }

    public enum Method{
        GET("GET")
        ,PUT("PUT")
        ,POST("POST")
        ;
        private String name;

        Method(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
