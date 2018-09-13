package com.hieupt.kotlin.retrofit2.queue

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

object RetrofitQueue {

    interface SyncRequestCallback<T> {

        fun onResponse(request: Call<T>, responseData: T?) {

        }

        fun onFailure(request: Call<T>, throwable: Throwable?) {

        }
    }

    const val DEFAULT_MAX_REQUEST_ACTIVE = 1

    private val requestQueue: LinkedList<Request<*>> = LinkedList()

    var maxRequestActive = DEFAULT_MAX_REQUEST_ACTIVE

    fun <T> addRequest(request: Call<T>, callback: Callback<T>?) {
        val requestWrapper = Request(request, callback)
        requestQueue.add(requestWrapper)
        executeRequestIfOk(requestWrapper)
    }

    fun <T> addRequestToFrontQueue(request: Call<T>, callback: Callback<T>?) {
        val requestWrapper = Request(request, callback)
        requestQueue.addFirst(requestWrapper)
        executeRequestIfOk(requestWrapper)
    }

    fun <T> enqueue(request: Call<T>, callback: Callback<T>) {
        request.enqueue(callback)
    }

    fun <T> executeRequestSync(request: Call<T>): T? {
        return executeRequestSync(request, null)
    }

    fun <T> executeRequestSync(request: Call<T>, callback: SyncRequestCallback<T>?): T? {
        return try {
            val response = request.execute()
            val responseData = response.body()
            callback?.let {
                if (response.isSuccessful) {
                    callback.onResponse(request, responseData)
                } else {
                    callback.onFailure(request, null)
                }
            }
            responseData
        } catch (e: IOException) {
            callback?.onFailure(request, e)
            null
        }
    }

    private fun <T> executeRequestIfOk(request: Request<T>) {
        if (requestQueue.size <= maxRequestActive) {
            request.execute()
        }
    }

    class Request<T>(private val request: Call<T>, val callback: Callback<T>?) {

        var isExecuted = false

        fun execute() {
            isExecuted = true
            request.enqueue(CallbackDecorator(this))
        }
    }

    class CallbackDecorator<T>(private val request: Request<T>) : Callback<T> {

        override fun onFailure(call: Call<T>, t: Throwable) {
            request.callback?.onFailure(call, t)
            performNextRequest()
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            request.callback?.onResponse(call, response)
            performNextRequest()
        }

        private fun performNextRequest() {
            requestQueue.remove(request)
            if (!requestQueue.isEmpty()) {
                val request = requestQueue.firstOrNull {
                    !it.isExecuted
                }
                request?.execute()
            }
        }
    }
}