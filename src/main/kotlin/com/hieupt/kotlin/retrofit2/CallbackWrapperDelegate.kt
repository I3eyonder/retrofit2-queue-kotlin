package com.hieupt.kotlin.retrofit2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal open class CallbackWrapperDelegate<T>(private val delegate: Callback<T>?) : CallbackWrapper<T>() {

    override fun onResponse(call: Call<T>, response: Response<T>, responseData: T?) {
        delegate?.onResponse(call, response)
    }

    override fun onFailure(call: Call<T>, isCanceled: Boolean, t: Throwable) {
        delegate?.onFailure(call, t)
    }
}
