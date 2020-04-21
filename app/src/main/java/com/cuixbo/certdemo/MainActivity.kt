package com.cuixbo.certdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.BufferedInputStream
import java.io.BufferedReader

import java.io.InputStreamReader

import java.lang.StringBuilder
import java.net.URL
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class MainActivity : AppCompatActivity() {

    var certService: CertService = RetrofitManager.instance.create(CertService::class.java)

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCertExpireTime(R.raw.jianshu)
        getSSLSocketFactory()
        // testOKHttpApi()
        testHttpsUrlConnectionApi()
    }

    /**
     * 获取CA证书的过期时间
     */
    private fun getCertExpireTime(certRes: Int): Date? {
        try {
            // 取到证书的输入流
            val input = resources.openRawResource(certRes)
            val alias = "jianshu"
            val certificate = CertificateFactory.getInstance("X.509")
                .generateCertificate(input) as X509Certificate
            // 创建 Keystore 包含我们的证书
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry(alias, certificate)
            Log.e("xbc", "notAfter:" + certificate.notAfter.toLocaleString())
            return certificate.notAfter
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getSSLSocketFactory(): SSLSocketFactory? {
        try {
            // 取到证书的输入流
//            val input = resources.openRawResource(R.raw.jianshu)
            val input = resources.openRawResource(R.raw.jianshu_center)
            val alias = "jianshu"
            val certificate = CertificateFactory.getInstance("X.509").generateCertificate(input)

            // 创建 Keystore 包含我们的证书
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry(alias, certificate)

            // 使用默认算法 创建一个 TrustManager 仅把 Keystore 中的证书 作为信任的锚点
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)

            // 用 TrustManager 初始化一个 SSLContext
            val sslContext = SSLContext.getInstance("TLS")
            // 定义：public static SSLContext ssl_ctx = null;
            sslContext.init(null, tmf.trustManagers, null)
            return sslContext.socketFactory

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun testOKHttpApi() {
        val disposable: Disposable = certService.getInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.e("xbc", it.toString()) },
                { Log.e("xbc", it.toString()) }
            )
        compositeDisposable.add(disposable)
    }

    private fun testHttpsUrlConnectionApi() {
        val disposable: Disposable = Observable.create(
            ObservableOnSubscribe<String> {
                val apiUrl = "https://www.jianshu.com/p/19f311d81b6d"
                val url = URL(apiUrl)
                val conn = url.openConnection() as HttpsURLConnection
                conn.sslSocketFactory = getSSLSocketFactory()
                conn.requestMethod = "GET"
                conn.connect()
                val input = conn.inputStream
                val statusCode = conn.responseCode
                var result = ""
                if (statusCode == HttpsURLConnection.HTTP_OK) {
                    try {
                        val stringBuilder = StringBuilder()
                        // read by reader
//                        val reader = BufferedReader(InputStreamReader(input, "utf-8"))
//                        var buffer: String? = reader.readLine()
//                        while (buffer != null) {
//                            stringBuilder.append(buffer).append("\r\n")
//                            buffer = reader.readLine()
//                        }

                        // read by InputStream
//                        val bis = BufferedInputStream(input)
//                        val byteArray: ByteArray = ByteArray(1024)
//                        var len = bis.read(byteArray)
//                        while (len != -1) {
//                            stringBuilder.append(String(byteArray, Charset.forName("utf-8")))
//                            len = bis.read(byteArray)
//                        }

                        // read with kotlin
                        BufferedReader(InputStreamReader(input, "utf-8")).readLine()
                            .forEach { line ->
                                stringBuilder.append(line)
                            }
                        result = stringBuilder.toString()
                        conn.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result = e.localizedMessage
                        // Log.e("xbc", "Exception:${e.localizedMessage}")
                    }
                }
                it.onNext(result)
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.e("xbc", "result:${it.toString()}") },
                { Log.e("xbc", "result:${it.toString()}") }
            )
        compositeDisposable.add(disposable)
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}
