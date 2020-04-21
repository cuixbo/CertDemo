package com.cuixbo.certdemo

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * @author xiaobocui
 * @date 2020/4/21
 */
interface CertService {

    @GET("p/19f311d81b6d")
    fun getInfo(): Observable<String>

}