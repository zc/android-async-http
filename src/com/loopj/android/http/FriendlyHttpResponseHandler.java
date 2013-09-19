/*
 Android Asynchronous Http Client
 "Friendly" HTTP Response handler detects response content type and
 dispatches to appropriate success and failure handlers.

 Also, default error handlers provide noisy error messages to users by default,
 rather than doing nothing.  eg., if the server goes down, pop up a dialog
 saying as much rather than calling an empty error handler.

 */

package com.loopj.android.http;


import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class FriendlyHttpResponseHandler extends
        com.loopj.android.http.AsyncHttpResponseHandler {

    Context context;

    public FriendlyHttpResponseHandler(Context startContext) {
        super();
        context = startContext;
    }

    @Override
    public void onFailure(Throwable e) {
        String errorDescription = "An unknown error occurred.";
        AlertDialog.Builder builder = new AlertDialog.Builder(
            context);
        builder.setTitle("Alert");
        builder.setMessage(errorDescription);
        builder.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    dialog.dismiss();
                }
            });
        builder.create().show();
    }

    public void onFailure(Throwable e, JSONObject errorResponse) {
        String errorDescription = "An unknown error occurred.";
        try {
            if (errorResponse.has("error")) {
                errorDescription = errorResponse.getString("error");
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(
            context);
        builder.setTitle("Alert");
        builder.setMessage(errorDescription);
        builder.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    dialog.dismiss();
                }
            });
        builder.create().show();
    }

    @Override
    public void onFailure(Throwable e, String errorResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
            context);
        builder.setTitle("Alert");
        builder.setMessage(errorResponse);
        builder.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    dialog.dismiss();
                }
            });
        builder.create().show();
    }

    public void onFailure(Throwable e, JSONArray errorResponse) {
        String errorDescription = "An unknown error occurred.";
        AlertDialog.Builder builder = new AlertDialog.Builder(
            context);
        builder.setTitle("Alert");
        builder.setMessage(errorDescription);
        builder.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    dialog.dismiss();
                }
            });
        builder.create().show();
    }

    public void onSuccess(JSONObject response) {
    }

    public void onSuccess(JSONArray response) {
    }

    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        onSuccess(response);
    }

    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        onSuccess(response);
    }

    protected void handleFailureMessage(Throwable e, Header[] headers,
            String responseBody) {
        if (headers != null) {
            for (Header header : headers) {
                // XXX Find a header parsing library
                if (header.getName().startsWith("Content-Type")
                        && header.getValue().contains("application/json")) {
                    try {
                        Object jsonResponse = parseResponse(responseBody);
                        handleFailureJsonMessage(headers, jsonResponse);
                        return;
                    } catch (JSONException e1) {
                        onFailure(e1, responseBody);
                    }
                }
            }
        }
        onFailure(e, responseBody);
    }

    protected void handleFailureJsonMessage(Header[] headers,
            Object jsonResponse) {
        if (jsonResponse instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonResponse;
            try {
                if (obj.has("error") && (obj.getString("error").length() > 0)) {
                    onFailure(
                            new JSONException("Error: ".concat(obj
                                    .getString("error"))), obj);
                }
            } catch (JSONException e) {
                // No error found
            }
        }
    }

    protected void handleSuccessMessage(int statusCode, Header[] headers,
            String responseBody) {
        for (Header header : headers) {
            if (header.getName().startsWith("Content-Type")
                    && header.getValue().contains("application/json")) {
                try {
                    Object jsonResponse = parseResponse(responseBody);
                    handleSuccessJsonMessage(statusCode, headers, jsonResponse);
                } catch (JSONException e) {
                    handleFailureMessage(e, headers, responseBody);
                }
            }
        }
        onSuccess(statusCode, headers, responseBody);
    }

    protected void handleSuccessJsonMessage(int statusCode, Header[] headers,
            Object jsonResponse) {
        if (jsonResponse instanceof JSONObject) {
            onSuccess(statusCode, headers, (JSONObject) jsonResponse);
        } else if (jsonResponse instanceof JSONArray) {
            onSuccess(statusCode, headers, (JSONArray) jsonResponse);
        } else {
            onFailure(new JSONException("Unexpected type "
                    + jsonResponse.getClass().getName()), (JSONObject) null);
        }
    }

    protected Object parseResponse(String responseBody) throws JSONException {
        Object result = null;
        // trim the string to prevent start with blank, and test if the string
        // is valid JSON, because the parser don't do this :(. If Json is not
        // valid this will return null
        responseBody = responseBody.trim();
        if (responseBody.startsWith("{") || responseBody.startsWith("[")) {
            result = new JSONTokener(responseBody).nextValue();
        }
        if (result == null) {
            result = responseBody;
        }
        return result;
    }
}

