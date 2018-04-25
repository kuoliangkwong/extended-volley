package com.kkl.extendedvolley;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuo Liang on 17-Apr-18.
 */

public class ExtendedVolley {

    private static ExtendedVolley mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private enum RequestType {
        STRING, FILE
    }
    private String mUrl;
    private String mFileDestination;
    private RequestType mRequestType;
    private Map<String, String> mHeaders;
    private Listener mListener;
    private Response.ErrorListener mErrorListener;

    public interface Listener {
        void onResponse(String result);
    }

    protected ExtendedVolley(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ExtendedVolley getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ExtendedVolley(context);
        }
        mInstance.clear();
        return mInstance;
    }

    protected Map<String, String> getHeaders() {
        return mHeaders;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    private synchronized void clear() {
        mUrl = null;
        mFileDestination = null;
        mRequestType = null;
        mHeaders = null;
        mListener = null;
        mErrorListener = null;
    }

    public ExtendedVolley load(String url) {
        mUrl = url;
        return this;
    }

    public ExtendedVolley asFile(String dest) {
        mRequestType = RequestType.FILE;
        mFileDestination = dest;
        return this;
    }

    public ExtendedVolley asString() {
        mRequestType = RequestType.STRING;
        return this;
    }

    public ExtendedVolley headers(Map<String, String> headers) {
        mHeaders = headers;
        return this;
    }

    public ExtendedVolley listener(Listener listener) {
        mListener = listener;
        return this;
    }

    public ExtendedVolley error(Response.ErrorListener errorListener) {
        mErrorListener = errorListener;
        return this;
    }

    public void get() {
        if (mRequestType == null) {
            throw new NullPointerException("You need to call asFile() or asString() before calling get()");
        }
        final Listener listener = mListener;
        final Response.ErrorListener errorListener = mErrorListener;
        switch (mRequestType) {
            case FILE: {
                InputStreamVolleyRequest request = getFileRequest(Request.Method.GET, mUrl, mHeaders,
                        new Response.Listener<byte[]>() {
                            @Override
                            public void onResponse(byte[] response) {
                                try {
                                    if (response != null) {
                                        FileOutputStream outputStream = new FileOutputStream(mFileDestination);
                                        outputStream.write(response);
                                        outputStream.close();
                                        notifyListener(listener, mFileDestination);
                                    } else {
                                        notifyErrorListener(errorListener, new NetworkError());
                                    }
                                } catch (Exception e) {
                                    notifyErrorListener(errorListener, new ParseError());
                                }
                            }
                        }, errorListener);
                addToRequestQueue(request);
                break;
            }
            case STRING: {
                StringRequest request = getStringRequest(Request.Method.GET, mUrl, mHeaders,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                notifyListener(listener, response);
                            }
                        }, errorListener);
                addToRequestQueue(request);
                break;
            }
        }
    }

    private <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private StringRequest getStringRequest(int method, String url,
                                           Map<String, String> headers,
                                           Response.Listener<String> listener,
                                           Response.ErrorListener errorListener) {
        final Map<String, String> headersCopy =
                headers != null ? new HashMap<>(headers) : new HashMap<String, String>();
        StringRequest stringRequest = new StringRequest(
                method, url,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headersCopy;
            }
        };
        return stringRequest;
    }

    private InputStreamVolleyRequest getFileRequest(int method, String url,
                                                    Map<String, String> headers,
                                                    Response.Listener<byte[]> listener,
                                                    Response.ErrorListener errorListener) {
        final Map<String, String> headersCopy =
                headers != null ? new HashMap<>(headers) : new HashMap<String, String>();
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(
                method, url, listener, errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headersCopy;
            }
        };
        return request;
    }

    private void notifyListener(Listener listener, String result) {
        if (listener != null) {
            listener.onResponse(result);
        }
    }

    private void notifyErrorListener(Response.ErrorListener errorListener, VolleyError error) {
        if (errorListener != null) {
            errorListener.onErrorResponse(error);
        }
    }

    class InputStreamVolleyRequest extends Request<byte[]> {
        private final Response.Listener<byte[]> mListener;

        public InputStreamVolleyRequest(int method, String mUrl ,Response.Listener<byte[]> listener,
                                        Response.ErrorListener errorListener) {
            // TODO Auto-generated constructor stub

            super(method, mUrl, errorListener);
            setShouldCache(false);
            mListener = listener;
        }

        @Override
        protected void deliverResponse(byte[] response) {
            mListener.onResponse(response);
        }

        @Override
        protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
            return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
        }
    }
}
