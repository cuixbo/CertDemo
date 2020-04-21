package com.cuixbo.certdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testCa()
    }

    private fun testCa() {
        try {
            // 取到证书的输入流
            val caInput = resources.openRawResource(R.raw.jianshu)
            val ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput)

            // 创建 Keystore 包含我们的证书
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            val alias = "jianshu"
            keyStore.setCertificateEntry(alias, ca)
            val certificate = keyStore.getCertificate(alias) as X509Certificate
            val notAfter = certificate.notAfter
            val notBefore = certificate.notAfter
            Log.e("xbc", "notAfter:" + notAfter.toLocaleString())
            Log.e("xbc", "notBefore:" + notBefore.toLocaleString())

            // 创建一个 TrustManager 仅把 Keystore 中的证书 作为信任的锚点
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)

            // 用 TrustManager 初始化一个 SSLContext
            val ssl_ctx = SSLContext.getInstance("TLS")
            // 定义：public static SSLContext ssl_ctx = null;
            ssl_ctx.init(null, tmf.trustManagers, SecureRandom())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
