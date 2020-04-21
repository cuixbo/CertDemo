package com.cuixbo.certdemo

import io.reactivex.Observable
import retrofit2.http.POST

/**
 * @author xiaobocui
 * @date 2020/4/21
 */
interface CertService {

    @POST("getInfo")
    fun getInfo(): Observable<String>?

}