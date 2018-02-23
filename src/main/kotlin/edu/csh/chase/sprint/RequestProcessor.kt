package edu.csh.chase.sprint

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import java.io.IOException
import okhttp3.Request as OkRequest
import okhttp3.Response as OkResponse

class RequestProcessor(val request: Request,
                       val client: OkHttpClient,
                       private val listener: SprintListener?,
                       val retryLimit: Int = 0) : Callback {

    private var attemptCount = 0

    private var sleepTime = 1

    private var currentCall: Call? = null

    private var executed = false

    @Deprecated("use asyncExecute", ReplaceWith("asyncExecute()"))
    fun executeRequest(): RequestProcessor {
        return asyncExecute()
    }

    fun asyncExecute(): RequestProcessor {
        if (executed) {
            return this
        }

        executed = true

        currentCall = client.newCall(request.okHttpRequest)

        currentCall!!.enqueue(this)
        if (attemptCount == 0) {
            listener?.sprintRequestQueued(request)
        }
        return this
    }

    fun syncExecute(): Response {
        if (executed) {
            return Response.ConnectionError(request, IOException("Request has already been executed"))
        }

        executed = true

        currentCall = client.newCall(request.okHttpRequest)

        return try {
            val r = currentCall!!.execute()

            Response.Success(request, r.code(), r.body()?.use { it.bytes() }, r.headers())
        } catch (e: IOException) {
            Response.ConnectionError(request, e)
        }
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

    override fun onFailure(request: Call?, e: IOException) {
        if (attemptCount < retryLimit) {
            retry()
            return
        }

        listener?.sprintConnectionError(Response.ConnectionError(this.request, e))
    }

    override fun onResponse(request: Call, response: OkResponse) {
        with(response) {
            val code = response.code()
            if (code in 200..299) {
                listener?.sprintSuccess(
                    Response.Success(
                        request = this@RequestProcessor.request,
                        statusCode = code,
                        body = body()?.use { it.bytes() },
                        headers = headers()
                    )
                )
            } else {
                listener?.sprintFailure(
                    Response.Failure(
                        request = this@RequestProcessor.request,
                        statusCode = code,
                        body = body()?.use { it.bytes() },
                        headers = headers()
                    )
                )
            }
        }

    }

}