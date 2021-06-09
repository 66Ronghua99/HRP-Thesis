package com.ronghua.bledetect.network;

import android.os.Build;

import com.google.gson.Gson;
import com.ronghua.bledetect.authentication.Authentication;
import com.ronghua.bledetect.authentication.CsrHelper;
import com.ronghua.bledetect.network.responses.CertResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignRequestCallback implements Callback {

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        //pass
        System.out.println("Failure");
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Authentication auth = Authentication.getInstance();
        //here we can set the certificate;
        Gson gson = new Gson();
        CertResponse certResponse = gson.fromJson(Objects.requireNonNull(response.body()).string(), CertResponse.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CsrHelper.getInstance().setCertificate(Base64.getDecoder().decode(certResponse.getEncodedCsr()));
        } else {
            String data = new String(android.util.Base64.decode(certResponse.getEncodedCsr(), android.util.Base64.DEFAULT));
        }
    }
}
