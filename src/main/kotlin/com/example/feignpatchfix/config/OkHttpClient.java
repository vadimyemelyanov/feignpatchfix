package com.example.feignpatchfix.config;

import feign.AsyncClient;
import feign.Client;
import feign.Request.ProtocolVersion;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static feign.Util.enumForName;


public final class OkHttpClient implements Client, AsyncClient<Object> {

    private final okhttp3.OkHttpClient delegate;

    public OkHttpClient() {
        this(new okhttp3.OkHttpClient());
    }

    public OkHttpClient(okhttp3.OkHttpClient delegate) {
        this.delegate = delegate;
    }

    static Request toOkHttpRequest(feign.Request input) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(input.url());

        MediaType mediaType = null;
        boolean hasAcceptHeader = false;
        for (String field : input.headers().keySet()) {
            if (field.equalsIgnoreCase("Accept")) {
                hasAcceptHeader = true;
            }

            for (String value : input.headers().get(field)) {
                requestBuilder.addHeader(field, value);
                if (field.equalsIgnoreCase("Content-Type")) {
                    mediaType = MediaType.parse(value);
                    if (input.charset() != null) {
                        mediaType.charset(input.charset());
                    }
                }
            }
        }
        // Some servers choke on the default accept string.
        if (!hasAcceptHeader) {
            requestBuilder.addHeader("Accept", "*/*");
        }

        byte[] inputBody = input.body();

        if (input.httpMethod() == feign.Request.HttpMethod.POST
            || input.httpMethod() == feign.Request.HttpMethod.PUT
            || input.httpMethod() == feign.Request.HttpMethod.PATCH) {
            requestBuilder.removeHeader("Content-Type");
            if (inputBody == null) {
                inputBody = new byte[0];
            }
        }

        RequestBody body = inputBody != null ? RequestBody.create(mediaType, inputBody) : null;
        requestBuilder.method(input.httpMethod().name(), body);
        return requestBuilder.build();
    }

    private static feign.Response toFeignResponse(Response response, feign.Request request)
        throws IOException {
        return feign.Response.builder()
            .protocolVersion(enumForName(ProtocolVersion.class, response.protocol()))
            .status(response.code())
            .reason(response.message())
            .request(request)
            .headers(toMap(response.headers()))
            .body(toBody(response.body()))
            .build();
    }

    private static Map<String, Collection<String>> toMap(Headers headers) {
        return (Map) headers.toMultimap();
    }

    private static feign.Response.Body toBody(final ResponseBody input) throws IOException {
        if (input == null || input.contentLength() == 0) {
            if (input != null) {
                input.close();
            }
            return null;
        }
        final Integer length = input.contentLength() >= 0 && input.contentLength() <= Integer.MAX_VALUE
            ? (int) input.contentLength()
            : null;

        return new feign.Response.Body() {

            @Override
            public void close() throws IOException {
                input.close();
            }

            @Override
            public Integer length() {
                return length;
            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public InputStream asInputStream() throws IOException {
                return input.byteStream();
            }

            @SuppressWarnings("deprecation")
            @Override
            public Reader asReader() throws IOException {
                return input.charStream();
            }

            @Override
            public Reader asReader(Charset charset) throws IOException {
                return asReader();
            }
        };
    }

    private okhttp3.OkHttpClient getClient(feign.Request.Options options) {
        okhttp3.OkHttpClient requestScoped;
        if (delegate.connectTimeoutMillis() != options.connectTimeoutMillis()
            || delegate.readTimeoutMillis() != options.readTimeoutMillis()
            || delegate.followRedirects() != options.isFollowRedirects()) {
            requestScoped = delegate.newBuilder()
                .connectTimeout(options.connectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(options.readTimeoutMillis(), TimeUnit.MILLISECONDS)
                .followRedirects(options.isFollowRedirects())
                .build();
        } else {
            requestScoped = delegate;
        }
        return requestScoped;
    }

    @Override
    public feign.Response execute(feign.Request input, feign.Request.Options options)
        throws IOException {
        okhttp3.OkHttpClient requestScoped = getClient(options);
        Request request = toOkHttpRequest(input);
        Response response = requestScoped.newCall(request).execute();
        return toFeignResponse(response, input).toBuilder().request(input).build();
    }

    @Override
    public CompletableFuture<feign.Response> execute(feign.Request input,
                                                     feign.Request.Options options,
                                                     Optional<Object> requestContext) {
        okhttp3.OkHttpClient requestScoped = getClient(options);
        Request request = toOkHttpRequest(input);
        CompletableFuture<feign.Response> responseFuture = new CompletableFuture<>();
        requestScoped.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response)
                throws IOException {
                final feign.Response r =
                    toFeignResponse(response, input).toBuilder().request(input).build();
                responseFuture.complete(r);
            }
        });
        return responseFuture;
    }
}