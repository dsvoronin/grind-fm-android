package com.dsvoronin.grindfm.network;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.util.Map;

public class GrindRequest extends Request<String> {

    /**
     * 2 min
     */
    private static final long DEFAULT_CACHE_TTL = 40 * 60 * 1000;

    private static final long DEFAULT_CACHE_SOFT_TTL = 1 * 60 * 1000;

    private final Response.Listener<String> mListener;

    private long cacheTTL;

    private long cacheSoftTTL;

    public GrindRequest(String url, Response.Listener<String> mListener, Response.ErrorListener errorListener) {
        this(url, mListener, errorListener, DEFAULT_CACHE_TTL, DEFAULT_CACHE_SOFT_TTL);
    }

    public GrindRequest(String url, Response.Listener<String> mListener, Response.ErrorListener listener, long cacheTTL, long cacheSoftTTL) {
        super(Method.GET, url, listener);
        this.mListener = mListener;
        this.cacheSoftTTL = cacheSoftTTL;
        this.cacheTTL = cacheTTL;
    }

    /**
     * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
     * Cache-control headers are ignored. SoftTtl == 3 mins, ttl == 24 hours.
     *
     * @param response The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is not cacheable.
     */
    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response, long cacheTTL, long cacheSoftTTL) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        final long softExpire = now + cacheSoftTTL;
        final long ttl = now + cacheTTL;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success(new String(response.data), parseIgnoreCacheHeaders(response, cacheTTL, cacheSoftTTL));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
