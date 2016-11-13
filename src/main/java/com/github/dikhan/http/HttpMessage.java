package com.github.dikhan.http;

import com.github.dikhan.exceptions.HttpMessageParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class HttpMessage {

    private final List<String> HTTP_VERSIONS_SUPPORTED = Arrays.asList("HTTP/1.0", "HTTP/1.1");
    private static final String CRLF = "\r\n";

    private String httpMethod;
    private URI url;
    private String httpVersion;
    private Map<String, List<String>> httpHeaders = new HashMap<>();
    private byte[] httpBodyContent;

    public void parseRequestLine(String requestLine) throws HttpMessageParseException {
        try {
            String[] requestLineValues = requestLine.split(" ");
            if(requestLineValues.length != 3) {
                throw new HttpMessageParseException("HTTP request line must contain HttpMethod, URI and HttpVersion - " + requestLine);
            }
            parseHttpMethod(requestLineValues[0]);
            parseUrl(requestLineValues[1]);
            parseHttpVersion(requestLineValues[2]);
        } catch(Exception ex) {
            throw new HttpMessageParseException("Incorrect HTTP request line - " + requestLine, ex);
        }
    }

    public void addHeader(String header) throws HttpMessageParseException {
        if(header == null) {
            throw new HttpMessageParseException("HTTP header can not be null");
        }
        String[] headerKeyValue = header.split(":");
        if(headerKeyValue.length != 2 || headerKeyValue[0].isEmpty() || headerKeyValue[1].isEmpty()) {
            throw new HttpMessageParseException("Incorrect HTTP header - " + header);
        } else {
            String headerKey = headerKeyValue[0].trim();
            if(!httpHeaders.containsKey(headerKey)) {
                httpHeaders.put(headerKey, new ArrayList<>());
            }
            List<String> headerValues = httpHeaders.get(headerKey);
            headerValues.add(headerKeyValue[1].trim());
        }
    }

    public List<String> getHeaderValues(String header) {
        return httpHeaders.get(header);
    }

    private void parseHttpMethod(String method) {
        this.httpMethod = HttpMethod.valueOf(method).toString();
    }

    private void parseUrl(String url) throws URISyntaxException {
        this.url = new URI(url);
    }

    private void parseHttpVersion(String httpVersion) throws HttpMessageParseException {
        if(HTTP_VERSIONS_SUPPORTED.contains(httpVersion)) {
            this.httpVersion = httpVersion;
        } else {
            throw new HttpMessageParseException("Incorrect HTTP version - " + httpVersion);
        }
    }

    public String getRequestLine() {
        return httpMethod + " " + url.toString() + " " + httpVersion + CRLF;
    }

    public String buildRequest() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRequestLine());
        for(String header : httpHeaders.keySet()) {
            for(String value : httpHeaders.get(header)) {
                sb.append(header).append(": ").append(value).append(CRLF);
            }
        }
        sb.append(CRLF);
        if(shouldProcessHttpBody()) {
            if(httpBodyContent.length > 0) {
                sb.append(new String(httpBodyContent));
                sb.append(CRLF);
            }
        }
        return sb.toString();
    }

    public boolean shouldProcessHttpBody() {
        return httpMethod.equals(HttpMethod.POST.toString()) || httpMethod.equals(HttpMethod.PUT.toString());
    }

    public void setHttpBodyContent(byte[] bodyContent) {
        httpBodyContent = bodyContent;
    }

    public URI getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Integer getContentLength() throws HttpMessageParseException {
        List<String> contentLengthValue = httpHeaders.get("Content-Length");
        if(contentLengthValue != null && !contentLengthValue.isEmpty()) {
            return Integer.valueOf(httpHeaders.get("Content-Length").get(0).trim());
        } else {
            throw new HttpMessageParseException("Content-Length header not found");
        }
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, List<String>> getHttpHeaders() {
        return httpHeaders;
    }
}
