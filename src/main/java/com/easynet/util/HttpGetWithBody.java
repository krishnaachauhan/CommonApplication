package com.easynet.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 *
 * @author mahen
 */
public class HttpGetWithBody extends HttpEntityEnclosingRequestBase{

    @Override
    public String getMethod() {
        return "GET";
    }
}
