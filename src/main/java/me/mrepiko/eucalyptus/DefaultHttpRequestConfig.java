package me.mrepiko.eucalyptus;

import lombok.Getter;
import lombok.Setter;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public final class DefaultHttpRequestConfig {
    private DefaultHttpRequestConfig() {}

    @Setter
    @Getter
    private static volatile OkHttpClient defaultClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(35, 5, TimeUnit.MINUTES))
            .build();

    @Setter
    @Getter
    private static volatile MediaType defaultMediaType = MediaType.parse("application/json; charset=utf-8");
}
