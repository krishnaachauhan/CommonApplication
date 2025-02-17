package com.easynet.impl;

import java.io.InputStream;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONTokenerImpl extends JSONTokener {

	public JSONTokenerImpl(InputStream inputStream) {
		super(inputStream);
		// TODO Auto-generated constructor stub
	}
	
	public JSONTokenerImpl(Reader reader) {
		super(reader);
		// TODO Auto-generated constructor stub
	}

	public JSONTokenerImpl(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	@Override
	public char nextClean() throws JSONException {
		// TODO Auto-generated method stub
		return super.nextClean();
	}
	
	/**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
	@Override
    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        String string;

        switch (c) {
        case '"':
        case '\'':
            return this.nextString(c);
        case '{':
            this.back();
            return new JSONObjectImpl(this);
        case '[':
            this.back();
            return new JSONArrayImpl(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        this.back();

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }
	
	
}

