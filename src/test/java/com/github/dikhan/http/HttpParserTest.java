package com.github.dikhan.http;

import com.github.dikhan.exceptions.HttpMessageParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class HttpParserTest {

    private static final String CRLF = "\r\n";
    private HttpParser httpParser;

    @Test
    public void parseHttpRequest() throws IOException, HttpMessageParseException, URISyntaxException {

        StringBuilder sb = new StringBuilder();
        sb.append("GET http://www.bbc.co.uk/ HTTP/1.1" + CRLF);
        sb.append("Host: www.bbc.co.uk" + CRLF);
        sb.append("User-Agent: curl/7.43.0" + CRLF);
        sb.append("Accept: */*" + CRLF);
        sb.append(CRLF);

        InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.US_ASCII));
        httpParser = new HttpParser(in);
        HttpMessage httpMessage = httpParser.parse();

        Assert.assertEquals("GET", httpMessage.getHttpMethod());
        Assert.assertEquals(new URI("http://www.bbc.co.uk/"), httpMessage.getUrl());
        Assert.assertEquals("HTTP/1.1", httpMessage.getHttpVersion());

        Assert.assertEquals(3, httpMessage.getHttpHeaders().size());
        Assert.assertNotNull(httpMessage.getHttpHeaders().get("Host"));
        Assert.assertNotNull(httpMessage.getHttpHeaders().get("User-Agent"));
        Assert.assertNotNull(httpMessage.getHttpHeaders().get("Accept"));

    }

    @Test(expected = HttpMessageParseException.class)
    public void requestLineDoesNotHaveCrLf() throws IOException, HttpMessageParseException, URISyntaxException {

        StringBuilder sb = new StringBuilder();
        sb.append("GET http://www.bbc.co.uk/ HTTP/1.1");

        InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.US_ASCII));
        httpParser = new HttpParser(in);
        httpParser.parse();
    }

}