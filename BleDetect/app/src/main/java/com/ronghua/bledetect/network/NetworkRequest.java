package com.ronghua.bledetect.network;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.ronghua.bledetect.authentication.CsrHelper;
import com.ronghua.bledetect.network.requests.CertRequest;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetworkRequest {
    private static NetworkRequest instance;
    public final static String CA_URL = "http://192.168.3.10:8080/sign/csr";

    public static NetworkRequest getInstance() {
        if(instance == null){
            instance = new NetworkRequest();
        }
        return instance;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestCertificate(String url){
        String csr = "";
        try {
            csr = CsrHelper.getInstance().getBase64Csr();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CertRequest request = new CertRequest();
        request.setUsername("ronghua@kth.se");
        request.setEncodedCsr(csr);
        Gson gson = new Gson();
        String json = gson.toJson(request, CertRequest.class);

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), json);
        Call call = constructPostCall(url, requestBody);
        call.enqueue(new SignRequestCallback());
    }


    private Call constructGetCall(String url){
        OkHttpClient httpClient = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = httpClient.newCall(getRequest);
        return call;
    }

    private Call constructPostCall(String url, RequestBody requestBody){
        OkHttpClient httpClient = new OkHttpClient();
        Request postRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = httpClient.newCall(postRequest);
        return call;
    }

}
