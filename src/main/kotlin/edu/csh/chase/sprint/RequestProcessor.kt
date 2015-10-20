package edu.csh.chase.sprint

import com.squareup.okhttp.*
import java.io.IOException
import com.squareup.okhttp.Request as OkRequest
import com.squareup.okhttp.Response as OkResponse

class RequestProcessor(val request: Request, private val client: OkHttpClient, private val listener: SprintListener?,
                       val retryLimit: Int = 0) :
        Callback {

    private var attemptCount = 0

    private var sleepTime = 1

    private var currentCall: Call? = null

    fun buildOkRequest(): OkRequest {
        val builder = OkRequest.Builder()
        //TODO make sure the URL is still valid if the urlParams is null
        builder.url(if (request.urlParams != null) {
            request.url + request.urlParams.toString()
        } else {
            request.url
        })
        when (request.requestType) {
            RequestType.Get -> builder.get()
            RequestType.Post -> builder.post(request.body ?: RequestBody.create(MediaType.parse("text/plain"), ""))
            RequestType.Put -> builder.put(request.body ?: RequestBody.create(MediaType.parse("text/plain"), ""))
            RequestType.Delete -> builder.delete(request.body)
        }
        builder.headers(request.headers.build())

        return builder.build()
    }

    fun executeRequest(): RequestProcessor {
        val okRequest = buildOkRequest()
        currentCall = client.newCall(okRequest)
        currentCall!!.enqueue(this)
        if (attemptCount == 0) {
            listener?.sprintRequestQueued(request)
        }
        return this
    }

    fun cancelRequest() {
        currentCall?.cancel()
        listener?.sprintRequestCanceled(request)
        currentCall = null
    }

    private fun retry() {
        attemptCount++
        Thread.sleep((sleepTime * 1000).toLong())
        sleepTime *= 2
    }

    override fun onFailure(request: OkRequest?, e: IOException) {
        if (attemptCount < retryLimit) {
            retry()
            return
        }
        //TODO create status codes for all potential IOExceptions
        listener?.sprintFailure(this.request, Response(-1, null, null))
    }

    override fun onResponse(response: OkResponse) {
        val statusCode = response.code()
        val body = response.body().bytes()
        val headers = response.headers()
        if (statusCode in 200..299) {
            listener?.sprintSuccess(request, Response(statusCode, body, headers))
        } else {
            listener?.sprintFailure(request, Response(statusCode, body, headers))
        }
    }

}