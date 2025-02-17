package com.easynet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

public class CustomContentCachingResponseWrapper extends ContentCachingResponseWrapper {
	
	private final FastByteArrayOutputStream content = new FastByteArrayOutputStream(1024);

	@Nullable
	private ServletOutputStream outputStream;

	@Nullable
	private PrintWriter writer;

	@Nullable
	private Integer contentLength;
	
	private Integer status;

	private HashMap<String,String> headers = new HashMap();
	/**
	 * Create a new ContentCachingResponseWrapper for the given servlet response.
	 * @param response the original servlet response
	 */
	public CustomContentCachingResponseWrapper(HttpServletResponse response) {
		super(response);	
		
		//set the previous header values,because previous header value not set directly.
		response.getHeaderNames().stream().forEach(action -> {
			headers.put(action, response.getHeader(action));			
		});
	}
	
	@Override
	public void setHeader(String name, String value) {
		headers.put(name, value);
		super.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		headers.put(name, value);
		super.addHeader(name, value);
	}

	@Override
	public String getHeader(String name) {
		if(headers.get(name)==null) {
			return super.getHeader(name);		
		}else {
			return headers.get(name);
		}
	}

	@Override
	public Collection<String> getHeaders(String name) {
		
		if(headers.get(name)==null) {					
			return super.getHeaders(name);
		}else {			
			HashSet<String> headerValues=new HashSet<>();
			headerValues.add(headers.get(name));			
			return headerValues;
		}
	}

	@Override
	public Collection<String> getHeaderNames() {
		
		Set<String> headerKeys=headers.keySet();
		headerKeys.addAll(super.getHeaderNames());
		return headerKeys;
	}

	@Override
	public void setStatus(int sc) {
		status=sc;
		super.setStatus(sc);
	}

	@Override
	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub
		super.setStatus(sc, sm);
	}

	@Override
	public int getStatus() {
		if(status==null) {
			return super.getStatus();
		}else {
			return status;
		}
	}

	@Override
	public void sendError(int sc) throws IOException {
		copyBodyToResponse(false);
		try {
			super.sendError(sc);
		}
		catch (IllegalStateException ex) {
			// Possibly on Tomcat when called too late: fall back to silent setStatus
			super.setStatus(sc);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void sendError(int sc, String msg) throws IOException {
		copyBodyToResponse(false);
		try {
			super.sendError(sc, msg);
		}
		catch (IllegalStateException ex) {
			// Possibly on Tomcat when called too late: fall back to silent setStatus
			super.setStatus(sc, msg);
		}
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		copyBodyToResponse(false);
		super.sendRedirect(location);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (this.outputStream == null) {
			this.outputStream = new ResponseServletOutputStream(getResponse().getOutputStream());
		}
		return this.outputStream;		
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (this.writer == null) {
			String characterEncoding = getCharacterEncoding();
			this.writer = (characterEncoding != null ? new ResponsePrintWriter(characterEncoding) :
					new ResponsePrintWriter(WebUtils.DEFAULT_CHARACTER_ENCODING));
		}
		return this.writer;
	}

	@Override
	public void flushBuffer() throws IOException {
		// do not flush the underlying response as the content as not been copied to it yet
	}

	@Override
	public void setContentLength(int len) {
		if (len > this.content.size()) {
			this.content.resize(len);
		}
		this.contentLength = len;
	}

	// Overrides Servlet 3.1 setContentLengthLong(long) at runtime
	@Override
	public void setContentLengthLong(long len) {
		if (len > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Content-Length exceeds ContentCachingResponseWrapper's maximum (" +
					Integer.MAX_VALUE + "): " + len);
		}
		int lenInt = (int) len;
		if (lenInt > this.content.size()) {
			this.content.resize(lenInt);
		}
		this.contentLength = lenInt;
	}

	@Override
	public void setBufferSize(int size) {
		if (size > this.content.size()) {
			this.content.resize(size);		
		}
	}

	@Override
	public void resetBuffer() {
		this.content.reset();
	}

	@Override
	public void reset() {
		if(!super.isCommitted()) {
			super.reset();			
		}
		this.content.reset();
	}

	/**
	 * Return the status code as specified on the response.
	 * @deprecated as of 5.2 in favor of {@link HttpServletResponse#getStatus()}
	 */
	@Deprecated
	public int getStatusCode() {
		return getStatus();
	}

	/**
	 * Return the cached response content as a byte array.
	 */
	public byte[] getContentAsByteArray() {
		return this.content.toByteArray();
	}

	/**
	 * Return an {@link InputStream} to the cached content.
	 * @since 4.2
	 */
	public InputStream getContentInputStream() {
		return this.content.getInputStream();
	}

	/**
	 * Return the current size of the cached content.
	 * @since 4.2
	 */
	public int getContentSize() {
		return this.content.size();
	}

	/**
	 * Copy the complete cached body content to the response.
	 * @since 4.2
	 */
	public void copyBodyToResponse(String ls_mediaType) throws IOException {
		copyBodyToResponse(true,ls_mediaType);
	}

	/**
	 * Copy the cached body content to the response.
	 * @param complete whether to set a corresponding content length
	 * for the complete cached body content
	 * @since 4.2
	 */
	protected void copyBodyToResponse(boolean complete,String ls_mediaType) throws IOException {
		if (this.content.size() > 0) {
			HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
			if ((complete || this.contentLength != null) && !rawResponse.isCommitted()) {
				rawResponse.setContentLength(complete ? this.content.size() : this.contentLength);				
				rawResponse.setContentType(ls_mediaType);
				
				this.contentLength = null;
			}			
			
			//set the previous header values,because previous header value not set directly.
			headers.forEach((key,value) ->{
				rawResponse.addHeader(key,value);			
			});
																
			rawResponse.setStatus(getStatus());
			this.content.writeTo(rawResponse.getOutputStream());
			
			this.content.reset();
			if (complete) {
				super.flushBuffer();
			}			
		}
	}

	private class ResponseServletOutputStream extends ServletOutputStream {

		private final ServletOutputStream os;

		public ResponseServletOutputStream(ServletOutputStream os) {
			this.os = os;
		}

		@Override
		public void write(int b) throws IOException {
			content.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			content.write(b, off, len);
		}

		@Override
		public boolean isReady() {
			return this.os.isReady();
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			this.os.setWriteListener(writeListener);
		}
	}


	private class ResponsePrintWriter extends PrintWriter {

		public ResponsePrintWriter(String characterEncoding) throws UnsupportedEncodingException {
			super(new OutputStreamWriter(content, characterEncoding));
		}

		@Override
		public void write(char[] buf, int off, int len) {
			super.write(buf, off, len);
			super.flush();
		}

		@Override
		public void write(String s, int off, int len) {
			super.write(s, off, len);
			super.flush();
		}

		@Override
		public void write(int c) {
			super.write(c);
			super.flush();
		}
	}
	
	public void of_set_data(String res_data)
	{		
		try {
			ResponseServletOutputStream new_write=new ResponseServletOutputStream(getOutputStream());
			reset();
			new_write.write(res_data.getBytes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
