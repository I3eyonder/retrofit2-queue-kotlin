package com.hieupt.kotlin.retrofit2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class CallbackWrapper<T> : Callback<T> {

    final override fun onResponse(call: Call<T>, response: Response<T>) {
        onResponse(call, response, response.body())
    }

    final override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure(call, call.isCanceled, t)
    }

    abstract fun onResponse(call: Call<T>, response: Response<T>, responseData: T?)

    abstract fun onFailure(call: Call<T>, isCanceled: Boolean, t: Throwable)
}
