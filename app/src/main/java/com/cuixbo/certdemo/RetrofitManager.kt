package com.cuixbo.certdemo

import com.google.gson.GsonBuilder
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author xiaobocui
 * @date 2020-01-19
 */
class RetrofitManager private constructor() {
    companion object {
        private const val DEFAULT_CONNECT_TIME_OUT: Long = 30
        private const val DEFAULT_READ_TIME_OUT: Long = 30
        private const val DEFAULT_WRITE_TIME_OUT: Long = 30

        private const val BASE_URL = "https://www.jianshu.com/";

        private const val CA_DOMAIN = "www.jianshu.com";
        const val CA_PUBLIC_KEY = "sha256/PEq+LkznQfqx4wWQRDcBCa7vG6WpapFfY945qBslBew=";
        const val CA_PUBLIC_KEY_CENTER =
            "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=";

//        private const val CA_DOMAIN = "www.sohu.com";

        val instance: RetrofitManager
            get() = SingletonHolder.retrofitManager
    }

    private object SingletonHolder {
        val retrofitManager = RetrofitManager()
    }

    private val mRetrofit: Retrofit

    init {
        // OkHttpClient配置
        val okHttpClient = OkHttpClient.Builder()
            // 连接超时时间
            .connectTimeout(DEFAULT_CONNECT_TIME_OUT, TimeUnit.SECONDS)
            // 读取超时时间
            .readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)
            // 写入超时时间
            .writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS)
            // 日志拦截器
//            .certificatePinner(
//                CertificatePinner.Builder()
////                    .add(CA_DOMAIN, CA_PUBLIC_KEY)
//                    .add(CA_DOMAIN, CA_PUBLIC_KEY_CENTER)
//                    .build()
//            )
//            .hostnameVerifier(JianShuHostnameVerifier())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        mRetrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
//            .baseUrl("https://www.sohu.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    /**
     * 获取Service实例
     */
    fun <T> create(tClass: Class<T>?): T {
        return mRetrofit.create(tClass)
    }

}