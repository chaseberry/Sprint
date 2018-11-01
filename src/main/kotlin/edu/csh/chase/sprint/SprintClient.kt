package edu.csh.chase.sprint

import edu.csh.chase.sprint.parameters.UrlParameters
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody

abstract class SprintClient(val urlBase: String) {

    private val client = OkHttpClient.Builder().let { configureClient(it); it.build() }

    abstract fun configureClient(client: OkHttpClient.Builder)

    abstract val defaultRequestSerializer: RequestSerializer

    open val defaultBackoffTimeout: BackoffTimeout
        get() = BackoffTimeout.Exponential(500, 2, 300000L, 6)

    open fun configureRequest(request: Request) {

    }

    private fun serializeBody(serializer: RequestSerializer?, body: Any?): RequestBody? {
        return if (serializer != null && serializer.isValidType(body)) {
            serializer.serialize(body)
        } else {
            defaultRequestSerializer.serialize(body)
        }
    }

    fun executeRequest(request: Request): Response {
        configureRequest(request)
        return RequestProcessor(request, client, null, defaultBackoffTimeout).syncExecute()
    }

    fun executeRequest(request: Request, listener: SprintListener?): RequestProcessor {
        configureRequest(request)
        return RequestProcessor(request, client, listener, defaultBackoffTimeout).asyncExecute()
    }

    fun executeRequest(request: Request, listener: RequestFinished?):
        RequestProcessor {

        return executeRequest(
            request = request,
            listener = object : SprintListener {
                override fun sprintFailure(response: Response.Failure) {
                    listener?.invoke(response)
                }

                override fun sprintSuccess(response: Response.Success) {
                    listener?.invoke(response)
                }

                override fun sprintConnectionError(response: Response.ConnectionError) {
                    listener?.invoke(response)
                }
            }
        )
    }

    fun get(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            extraData: Any? = null): Response {

        return executeRequest(
            GetRequest(
                url = buildEndpoint(urlBase, endpoint),
                urlParams = urlParameters,
                headers = headers,
                extraData = extraData
            )
        )
    }

    fun get(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            extraData: Any? = null, listener: SprintListener?): RequestProcessor {

        return executeRequest(GetRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData),
            listener = listener)
    }

    fun get(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            extraData: Any? = null, listener: RequestFinished?):
        RequestProcessor {

        return executeRequest(GetRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData),
            listener = listener)
    }

    fun post(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
             body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null): Response {

        return executeRequest(
            PostRequest(
                url = buildEndpoint(urlBase, endpoint),
                urlParams = urlParameters,
                headers = headers,
                extraData = extraData,
                body = serializeBody(serializer, body)
            )
        )
    }

    fun post(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
             body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null,
             listener: SprintListener? = null): RequestProcessor {

        return executeRequest(PostRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener)
    }

    fun post(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
             serializer: RequestSerializer? = null, body: Any? = null, extraData: Any? = null,
             listener: RequestFinished? = null): RequestProcessor {

        return executeRequest(PostRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener = listener)
    }

    fun put(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null): Response {

        return executeRequest(
            PutRequest(
                url = buildEndpoint(urlBase, endpoint),
                urlParams = urlParameters,
                headers = headers,
                extraData = extraData,
                body = serializeBody(serializer, body)
            )
        )
    }

    fun put(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null,
            listener: SprintListener? = null): RequestProcessor {

        return executeRequest(PutRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener)
    }

    fun put(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
            serializer: RequestSerializer? = null, body: Any? = null, extraData: Any? = null,
            listener: RequestFinished? = null): RequestProcessor {

        return executeRequest(PutRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener = listener)
    }

    fun delete(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
               body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null): Response {

        return executeRequest(
            DeleteRequest(
                url = buildEndpoint(urlBase, endpoint),
                urlParams = urlParameters,
                headers = headers,
                extraData = extraData,
                body = serializeBody(serializer, body)
            )
        )
    }

    fun delete(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
               body: Any? = null, serializer: RequestSerializer? = null, extraData: Any? = null,
               listener: SprintListener? = null): RequestProcessor {

        return executeRequest(DeleteRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener)
    }

    fun delete(endpoint: String, urlParameters: UrlParameters? = null, headers: Headers.Builder = Headers.Builder(),
               serializer: RequestSerializer? = null, body: Any? = null, extraData: Any? = null,
               listener: RequestFinished? = null): RequestProcessor {

        return executeRequest(DeleteRequest(
            url = buildEndpoint(urlBase, endpoint),
            urlParams = urlParameters,
            headers = headers,
            extraData = extraData,
            body = serializeBody(serializer, body)),
            listener = listener)
    }

}