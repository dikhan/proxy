package com.hexspeaks.http;

import com.hexspeaks.exceptions.HttpMessageParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class HttpMessageTest {

    private HttpMessage httpMessage;

    @Before
    public void setUp() {
        httpMessage = new HttpMessage();
    }

    @Test
    public void processRequestLine() throws URISyntaxException, HttpMessageParseException {
        String httpMethod = "GET";
        String uri = "http://www.bbc.co.uk/";
        String httpVersion = "HTTP/1.1";
        String reqLine = buildHttpRequestLine(httpMethod, uri, httpVersion);

        httpMessage.parseRequestLine(reqLine);

        Assert.assertEquals(httpMethod, httpMessage.getHttpMethod());
        Assert.assertEquals(new URI(uri), httpMessage.getUrl());
        Assert.assertEquals(httpVersion, httpMessage.getHttpVersion());
    }

    @Test(expected = HttpMessageParseException.class)
    public void processRequestLineWithUnknownHttpMethod() throws URISyntaxException, HttpMessageParseException {
        String reqLine = buildHttpRequestLine("UNKNOWN", "http://www.bbc.co.uk/", "HTTP/1.1");
        httpMessage.parseRequestLine(reqLine);
    }

    @Test(expected = HttpMessageParseException.class)
    public void processRequestLineWithWrongHttpVersion() throws URISyntaxException, HttpMessageParseException {
        String reqLine = buildHttpRequestLine("GET", "http://www.bbc.co.uk/", "HTTPWRONG/1.1");
        httpMessage.parseRequestLine(reqLine);
    }

    @Test(expected = HttpMessageParseException.class)
    public void processRequestLineMissingHttpMethod() throws URISyntaxException, HttpMessageParseException {
        String reqLine = "http://www.bbc.co.uk/ HTTP/1.1";
        httpMessage.parseRequestLine(reqLine);
    }

    @Test(expected = HttpMessageParseException.class)
    public void processRequestLineMissingHttpUri() throws URISyntaxException, HttpMessageParseException {
        String reqLine = "GET HTTP/1.1";
        httpMessage.parseRequestLine(reqLine);
    }

    @Test(expected = HttpMessageParseException.class)
    public void processRequestLineMissingHttpVersion() throws URISyntaxException, HttpMessageParseException {
        String reqLine = "GET /";
        httpMessage.parseRequestLine(reqLine);
    }

    @Test
    public void addHeader() throws HttpMessageParseException {
        String headerValue = "application/json";
        String header = "Content-type: " + headerValue;
        httpMessage.addHeader(header);

        List<String> headerValues = httpMessage.getHeaderValues("Content-type");

        Assert.assertEquals(1, headerValues.size());
        Assert.assertEquals(headerValue, headerValues.get(0));
    }

    @Test
    public void addHeaderValues() throws HttpMessageParseException {
        String headerValue = "value1";
        String headerValue2 = "value2";
        String header = "Header1: " + headerValue;
        String header2 = "Header1: " + headerValue2;

        httpMessage.addHeader(header);
        httpMessage.addHeader(header2);

        List<String> headerValues = httpMessage.getHeaderValues("Header1");

        Assert.assertEquals(2, headerValues.size());
        Assert.assertEquals(headerValue, headerValues.get(0));
        Assert.assertEquals(headerValue2, headerValues.get(1));
    }

    @Test(expected = HttpMessageParseException.class)
    public void addNullHeader() throws HttpMessageParseException {
        String header = null;
        httpMessage.addHeader(header);
    }

    @Test(expected = HttpMessageParseException.class)
    public void addBadHeader() throws HttpMessageParseException {
        String header = "Header - value";
        httpMessage.addHeader(header);
    }

    @Test(expected = HttpMessageParseException.class)
    public void addBadHeaderEmptyKey() throws HttpMessageParseException {
        String header = ": value";
        httpMessage.addHeader(header);
    }

    @Test(expected = HttpMessageParseException.class)
    public void addBadHeaderEmptyValue() throws HttpMessageParseException {
        String header = "Header:";
        httpMessage.addHeader(header);
    }

    @Test
    public void addHeaderWithLeadingAndTrailingSpaces() throws HttpMessageParseException {
        String header = "     Header:Value    ";

        httpMessage.addHeader(header);
        List<String> headerValues = httpMessage.getHeaderValues("Header");

        Assert.assertEquals(1, headerValues.size());
        Assert.assertEquals("Value", headerValues.get(0));
    }

    @Test
    public void returnContentLength() throws HttpMessageParseException {

        Integer contentLengthHeaderValue = 32;
        String contentLengthHeader = "Content-Length: " + contentLengthHeaderValue;

        httpMessage.addHeader("Header: HeaderValue");
        httpMessage.addHeader(contentLengthHeader);

        Assert.assertEquals(contentLengthHeaderValue, httpMessage.getContentLength());
    }

    @Test(expected = HttpMessageParseException.class)
    public void contentLengthDoesNotExist() throws HttpMessageParseException {
        httpMessage.addHeader("Header: HeaderValue");
        httpMessage.getContentLength();
    }

    private String buildHttpRequestLine(String httpMethod, String uri, String httpVersion) {
        return String.format("%s %s %s", httpMethod, uri, httpVersion);
    }

}