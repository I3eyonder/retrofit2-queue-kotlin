package com.hieupt.kotlin.retrofit2

import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

object RetrofitRequestSync {

    interface SyncRequestCallback<T> {

        fun onResponse(request: Call<T>, responseData: T?) {

        }

        fun onFailure(request: Call<T>, throwable: Throwable) {

        }
    }

    /**
     * Execute request on current thread
     *
     * @param request  request call
     * @param callback Callback for success or failure response
     * @param <T>      Type of response data
     * @return `T` or null if request failure
    </T> */
    fun <T> executeRequest(request: Call<T>, callback: SyncRequestCallback<T>? = null): T? {
        try {
            val response = request.execute()
            val responseData = response.body()
            callback?.let {
                if (response.isSuccessful) {
                    it.onResponse(request, responseData)
                } else {
                    it.onFailure(request, HttpException(response))
                }
            }
            return responseData
        } catch (e: IOException) {
            callback?.onFailure(request, e)
        }
        return null
    }
}
