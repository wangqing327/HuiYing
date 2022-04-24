package com.example.liuguangtv.utils;

import android.os.Environment;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {
   // private static final String TAG = "HttpUtils";
    private static volatile HttpUtils instance;
    private static Headers headers;
    private final OkHttpClient client;

    private HttpUtils() {
        File file = new File(Environment.getExternalStorageDirectory(), "cache11");

        client = new OkHttpClient().newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)   //设置读取超时时间
                .connectTimeout(60, TimeUnit.SECONDS) //设置连接的超时时间
                //不添加拦截器
                //.addInterceptor(getAppInterceptor())//Application拦截器
                .retryOnConnectionFailure(true)
                // .followRedirects(false)  //禁止OkHttp的重定向操作，我们自己处理重定向
                //.followSslRedirects(false)  //禁止OkHttp的重定向操作，我们自己处理重定向
                .cache(new Cache(file, 10 * 1024))
                .cookieJar(new LocalCookieJar())
                //https域名不验证证书
                .hostnameVerifier((hostname, session) -> true)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .sslSocketFactory(createSSLSocketFactory(), Objects.requireNonNull(getTrustManger()))
                //禁止使用代理，可以防止Fiddler工具抓包
                .proxy(Proxy.NO_PROXY)
                .build();

    }

    //单例okhttp
    public static HttpUtils getInstance() {
        if (instance == null) {
            synchronized (HttpUtils.class) {
                if (null == instance) {
                    instance = new HttpUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 实现X509TrustManager接口：
     */
    private static X509TrustManager getTrustManger() {
        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 方法createSSLSocketFactory()调用类TrustAllcert,获取SSLSocketFactory
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{getTrustManger()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        return ssfFactory;
    }

    private Interceptor getAppInterceptor() {
        //添加拦截器
        return chain -> {
            Request request = chain.request();
            Log.e("++++++++++", "拦截前:");
            //---------请求之前------------
            Response response = chain.proceed(request);
            Log.e("++++++++++", "拦截后:");
            //---------请求之后------------
            return response;
        };
    }

    public OkHttpClient getClient() {
        return client;
    }

    public HttpUtils addHeaders(Headers h) {
        headers = h;
        return this;
    }

    public String get(String url) throws Exception {
        Request request = new Request.Builder()
                .get()
                .headers(headers == null ? new Headers.Builder().build() : headers)
                .url(url)
                .build();
        // 3. 创建出一个Call对象
        Call call = client.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    /**
     * post请求
     *
     * @param url  网址
     * @param data k = 字段，v = 值
     */
    public String post(String url, Map<String, String> data) throws Exception {
        FormBody.Builder form = new FormBody.Builder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            form.add(key, value);
        }
        Request request = new Request.Builder()
                .post(form.build())
                .headers(headers == null ? new Headers.Builder().build() : headers)
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    /**
     * 自动保存/提交Cookie
     */
    static class LocalCookieJar implements CookieJar {
        List<Cookie> cookies;

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
            if (cookies != null) {
                return cookies;
            } else {
                return new ArrayList<>();
            }
        }

        @Override
        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
            this.cookies = list;
        }
    }
}
