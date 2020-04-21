package com.cuixbo.certdemo

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author xiaobocui
 * @date 2020-01-19
 */
class RetrofitManager private constructor() {
    companion object {
        private const val DEFAULT_CONNECT_TIME_OUT = 30
        private const val DEFAULT_READ_TIME_OUT = 30
        private const val DEFAULT_WRITE_TIME_OUT = 30
        private const val CA_DOMAIN = "www.jianshu.com";
        private const val CA_PUBLIC_KEY = "sha256/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        val instance: RetrofitManager
            get() = SingletonHolder.retrofitManager
    }

    private object SingletonHolder {
        val retrofitManager = RetrofitManager()
    }

    private val mRetrofit: Retrofit

    init {
        // OkHttpClient配置
        val okHttpClient = OkHttpClient.Builder() // 连接超时时间
            .connectTimeout(
                DEFAULT_CONNECT_TIME_OUT.toLong(),
                TimeUnit.SECONDS
            ) // 读取超时时间
            .readTimeout(
                DEFAULT_READ_TIME_OUT.toLong(),
                TimeUnit.SECONDS
            ) // 写入超时时间
            .writeTimeout(
                DEFAULT_WRITE_TIME_OUT.toLong(),
                TimeUnit.SECONDS
            ) // 日志拦截器
            .certificatePinner(
                CertificatePinner.Builder()
                    .add(CA_DOMAIN, CA_PUBLIC_KEY)
                    .build()
            )
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
        mRetrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("www.jianshu.com")
            .addConverterFactory(GsonConverterFactory.create())
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