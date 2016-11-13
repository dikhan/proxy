package com.hexspeaks.http;

import com.hexspeaks.exceptions.HttpMessageParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpParser {

    private final static Logger logger = LoggerFactory.getLogger(HttpParser.class);

    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final String CRLF = "\r\n";

    private enum HTTP_STATE {REQ_LINE, MESSAGE_HEADERS, MESSAGE_BODY, DONE}
    private HTTP_STATE currentState = HTTP_STATE.REQ_LINE;

    private final BufferedInputStream bufferedInputStream;

    private HttpMessage httpMessage;

    public HttpParser(InputStream in) {
        this.bufferedInputStream = new BufferedInputStream(in);
        this.httpMessage = new HttpMessage();
    }

    public HttpMessage parse() throws IOException, HttpMessageParseException {
        while(currentState != HTTP_STATE.DONE) {
            switch(currentState) {
                case REQ_LINE:
                    parseRequestLine();
                    currentState = HTTP_STATE.MESSAGE_HEADERS;
                    break;
                case MESSAGE_HEADERS:
                    parseHeaders();
                    currentState = HTTP_STATE.MESSAGE_BODY;
                    break;
                case MESSAGE_BODY:
                    if(httpMessage.shouldProcessHttpBody()) {
                        parseBody();
                    }
                    currentState = HTTP_STATE.DONE;
                    break;
            }
        }
        return httpMessage;
    }

    private void parseRequestLine() throws IOException, HttpMessageParseException {
        String line = readLine();
        httpMessage.parseRequestLine(line);
    }

    private void parseHeaders() throws IOException, HttpMessageParseException {
        String line = readLine();
        while(!line.isEmpty() && !carriageReturnLineFeed(line)) {
            httpMessage.addHeader(line);
            line = readLine();
        }
    }

    private void parseBody() throws IOException, HttpMessageParseException {
        byte[] body = new byte[httpMessage.getContentLength()];
        bufferedInputStream.read(body, 0, httpMessage.getContentLength());
        httpMessage.setHttpBodyContent(body);

        logger.debug("Http Body: " + new String(body));
    }

    private String readLine() throws IOException, HttpMessageParseException {
        StringBuilder sb = new StringBuilder();
        int current;
        int previous = 0;
        while((current = bufferedInputStream.read()) != -1) {
            sb.append((char) current);
            if(carriageReturnLineFeed(previous, current)) {
                break;
            }
            previous = current;
        }
        String line = sb.toString();
        if(!line.contains(CRLF)) {
            throw new HttpMessageParseException("HTTP line does not contain CRLF at the end - " + line);
        }
        logger.debug("Read line[" + line.replace(CRLF, "") + "]");
        return line.replace(CRLF, "");
    }

    private boolean carriageReturnLineFeed(int previous, int current) throws HttpMessageParseException {
        return previous != -1 && current != -1 ? (char)previous == CR && (char)current == LF : false;
    }

    private boolean carriageReturnLineFeed(String line) {
        return line.equals(CRLF);
    }

}
