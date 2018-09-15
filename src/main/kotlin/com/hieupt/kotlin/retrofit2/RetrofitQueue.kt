package com.hieupt.kotlin.retrofit2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RetrofitQueue(maxActiveRequest: Int = DEFAULT_MAX_REQUEST_ACTIVE) {

    private val activeList: FixedSizeArrayList<Request<*>> = FixedSizeArrayList(calculateMaxActiveRequest(maxActiveRequest))

    private val requestQueue: LinkedList<Request<*>> = LinkedList()

    /**
     * @return Current max active request number
     */
    val maxActiveRequest: Int
        @Synchronized get() = activeList.maxSize

    private val firstPendingRequest: Request<*>?
        get() = if (requestQueue.isNotEmpty()) requestQueue.peek() else null

    private fun calculateMaxActiveRequest(maxActiveRequest: Int): Int {
        var calculated = DEFAULT_MAX_REQUEST_ACTIVE
        if (maxActiveRequest >= DEFAULT_MAX_REQUEST_ACTIVE) {
            calculated = maxActiveRequest
        }
        return calculated
    }

    /**
     * Update max active request number and execute pending request if current active number lesser than max active request number
     *
     * @param maxActiveRequest Max active request number
     * @throws IllegalArgumentException If maxActiveRequest is lesser than 0
     */
    @Synchronized
    fun updateMaxActiveRequest(maxActiveRequest: Int) {
        activeList.maxSize = calculateMaxActiveRequest(maxActiveRequest)
        executeRemainAcceptableRequests()
    }

    private fun executeRemainAcceptableRequests() {
        while (requestQueue.isNotEmpty() && activeList.canAdd()) {
            tryToExecuteNextRequest()
        }
    }

    private fun tryToExecuteNextRequest() {
        if (activeList.canAdd()) {
            firstPendingRequest?.execute()
        }
    }

    /**
     * Add request to queue. Execute immediately if current active lesser max active
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    @Synchronized
    fun <T> addRequest(request: Call<T>, callback: Callback<T>?) {
        val requestWrap = Request(request, callback)
        requestQueue.add(requestWrap)
        tryToExecuteNextRequest()
    }

    /**
     * Add request to front of queue. Execute immediately if current active lesser max active
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    @Synchronized
    fun <T> addRequestToFrontQueue(request: Call<T>, callback: Callback<T>?) {
        val requestWrap = Request(request, callback)
        requestQueue.addFirst(requestWrap)
        tryToExecuteNextRequest()
    }

    /**
     * Execute request immediately
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    @Synchronized
    fun <T> requestNow(request: Call<T>, callback: Callback<T>?) {
        request.enqueue(CallbackWrapperDelegate(callback))
    }

    /**
     * Remove `request` from pending queue
     *
     * @param request Request need to remove
     */
    @Synchronized
    fun removeRequest(request: Call<*>) {
        requestQueue.removeIf { requestWrap -> requestWrap.request === request }
    }

    /**
     * Clear request queue. Executing request do not affect by this call.
     */
    @Synchronized
    fun clearQueue() {
        requestQueue.clear()
    }

    /**
     * Cancel `request` if it is activating
     *
     * @param request Request need to cancel
     */
    @Synchronized
    fun cancel(request: Call<*>) {
        val requestWrap = activeList.firstOrNull { it.request === request }
        requestWrap?.let {
            it.cancel()
            activeList.remove(it)
        }
    }

    /**
     * Cancel all activating requests.
     */
    @Synchronized
    fun cancel() {
        activeList.forEach {
            it.cancel()
        }
        activeList.clear()
    }

    /**
     * Cancel all activating requests and clear pending request queue also.
     */
    @Synchronized
    fun cancelAndClear() {
        cancel()
        clearQueue()
    }

    private inner class Request<T>(val request: Call<T>, val callback: Callback<T>?) {

        fun execute() {
            activeList.add(this)
            requestQueue.remove(this)
            request.enqueue(CallbackDecorator(this))
        }

        fun cancel() {
            request.cancel()
        }
    }

    private inner class CallbackDecorator<T>(val request: Request<T>) : CallbackWrapperDelegate<T>(request.callback) {

        override fun onResponse(call: Call<T>, response: Response<T>, responseData: T?) {
            super.onResponse(call, response, responseData)
            performNextRequest()
        }

        override fun onFailure(call: Call<T>, isCanceled: Boolean, t: Throwable) {
            super.onFailure(call, isCanceled, t)
            performNextRequest()
        }

        private fun performNextRequest() {
            activeList.remove(request)
            if (activeList.canAdd() && requestQueue.isNotEmpty()) {
                val request = requestQueue.peek()
                request?.execute()
            }
        }
    }

    companion object {

        const val DEFAULT_MAX_REQUEST_ACTIVE = 1
    }
}
