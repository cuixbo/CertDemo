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
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.CertificatePinner
import okio.ByteString
import java.io.InputStream
import java.net.URL
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*


/**
 *
 *  1.禁用Http明文传输，使用Https
 *  2.防中间人攻击
 *  3.防中间人Charles抓包
 *  4.证书预埋
 *  5.证书锁定-指纹
 *  6.证书过期
 *
 * @author xiaobocui
 * @date 2020-04-19
 */
class MainActivity : AppCompatActivity() {

    private var certService: CertService = RetrofitManager.instance.create(CertService::class.java)
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_text.setOnClickListener {
            testOKHttpApi()
//            testHttpsUrlConnectionApi()
        }

        getCertExpireTime(R.raw.jianshu)
//        getSSLSocketFactory()
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

            Log.e("xbc", "notBefore:" + certificate.notBefore.toLocaleString())
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
            sslContext.init(null, arrayOf(JianShuTrustManager()), null)
            return sslContext.socketFactory

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * SSL Pinning 获取证书
     * @return certificata
     */
    private fun getCertificate(): CertificatePinner? {
        var ca: Certificate? = null
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = resources.openRawResource(R.raw.jianshu)
            ca = try {
                cf.generateCertificate(caInput)
            } finally {
                caInput.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var certPin: String? = ""
        if (ca != null) {
            certPin = CertificatePinner.pin(ca)
        }
        return CertificatePinner.Builder()
            .add("jianshu", certPin)
            .build()
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
                conn.hostnameVerifier = JianShuHostnameVerifier()
                conn.connect()
                val input = conn.inputStream
                val statusCode = conn.responseCode
                var result = ""
                if (statusCode == HttpsURLConnection.HTTP_OK) {
                    // simple with kotlin
                    result = input.bufferedReader().use { reader -> reader.readText() }
                }
                conn.disconnect()
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

class JianShuTrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        TODO("Not yet implemented")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        //TODO("Not yet implemented")
        val pubkey = chain?.get(0)?.publicKey.toString()

        val cate = chain?.get(0)
        if (cate != null) {
            val base64 = CertificatePinner.pin(cate)
            Log.e("xbc", "base64:$base64");
        }

//        cate.signature
        Log.e("xbc", "pubkey:$pubkey");

    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }
}

class JianShuHostnameVerifier : HostnameVerifier {
    companion object {
        private val HOSTNAME_VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier()
    }

    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        Log.e("xbc", "JianShuHostnameVerifier.verify:$hostname");
        return HOSTNAME_VERIFIER.verify(hostname, session)
    }
}