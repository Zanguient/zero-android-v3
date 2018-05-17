package com.ekylibre.android.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ekylibre.android.LoginActivity;
import com.ekylibre.android.network.pojos.AccessToken;

import java.io.IOException;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;


public class ServiceGenerator {

    private static OkHttpClient.Builder httpClient;
    private static Retrofit.Builder builder;
    private static AccessToken mToken;

    /**
     * Class to retrive access_token for the first time
     */
    public static <S> S createService(Class<S> serviceClass) {

        Log.e("Service Generator", "Service Generator");

        httpClient = new OkHttpClient.Builder();

        builder = new Retrofit.Builder()
                .baseUrl(LoginActivity.OAUTH_URL)
                .addConverterFactory(MoshiConverterFactory.create());

//        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
//                .cipherSuites(
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
//                .build();

        OkHttpClient client = httpClient
                //.connectionSpecs(Collections.singletonList(spec))
                .build();

        Retrofit retrofit = builder.client(client).build();

        return retrofit.create(serviceClass);
    }


    public static <S> S createService(Class<S> serviceClass, AccessToken accessToken, Context context) {

        httpClient = new OkHttpClient.Builder();

        builder = new Retrofit.Builder()
                .baseUrl(LoginActivity.OAUTH_URL)
                .addConverterFactory(MoshiConverterFactory.create());

        if (accessToken != null) {
            mToken = accessToken;
            final AccessToken token = accessToken;
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-nature", "application/json")
                        .header("Authorization","Bearer " + token.getAccess_token())
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            httpClient.authenticator((route, response) -> {

                if (responseCount(response) >= 2) {
                    // If both the original call and the call with refreshed token failed,
                    // it will probably keep failing, so don't try again.
                    return null;
                }

                // We need a new client, since we don't want to make another call using our client with access token
                EkylibreAPI ekylibreAPI = createService(EkylibreAPI.class);
                Call<AccessToken> call = ekylibreAPI.getRefreshAccessToken(mToken.getRefresh_token());
                try {
                    retrofit2.Response<AccessToken> tokenResponse = call.execute();
                    if (tokenResponse.code() == 200) {
                        AccessToken newToken = tokenResponse.body();
                        mToken = newToken;
                        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("access_token", newToken.getAccess_token());
                        editor.putString("refresh_token", newToken.getRefresh_token());
                        editor.putInt("token_created_at", newToken.getCreated_at());
                        editor.putBoolean("is_authenticated", true);
                        editor.apply();

                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + newToken.getAccess_token())
                                .build();
                    } else {
                        return null;
                    }
                } catch(IOException e) {
                    return null;
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}
