package com.hexspeaks;

import com.hexspeaks.exceptions.HttpMessageParseException;
import com.hexspeaks.http.HttpMessage;
import com.hexspeaks.http.HttpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ClientTask.class);

    private final Socket clientSocket;

    public ClientTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
        logger.debug("New downstream connection accepted - " + clientSocket);
    }

    public void run() {
        Socket upstreamConnection = null;
        try {
            try (BufferedInputStream clientInput = new BufferedInputStream(clientSocket.getInputStream());
                 BufferedOutputStream clientOutput = new BufferedOutputStream(clientSocket.getOutputStream())) {

                HttpParser httpParser = new HttpParser(clientInput);
                HttpMessage httpMessage = httpParser.parse();

                boolean isSsl = (httpMessage.getUrl().getHost().contains("https") ? true : false);

                upstreamConnection = new Socket(httpMessage.getUrl().getHost(), isSsl ? 443 : 80);
                forwardRequestUpstream(upstreamConnection.getOutputStream(), httpMessage);
                forwardUpstreamResponseDownstream(clientOutput, upstreamConnection.getInputStream());

            } catch (HttpMessageParseException e) {
                e.printStackTrace();
            } finally {
                if (upstreamConnection != null) {
                    upstreamConnection.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardRequestUpstream(OutputStream upstreamOutputStream, HttpMessage httpMessage) throws IOException {
        byte[] httpRequest = httpMessage.buildRequest().getBytes(StandardCharsets.US_ASCII);
        upstreamOutputStream.write(httpRequest);
        upstreamOutputStream.flush();
    }

    private void forwardUpstreamResponseDownstream(OutputStream clientOutput, InputStream upstreamInputStream) throws IOException {
        int i;
        while ((i = upstreamInputStream.read()) != -1) {
            clientOutput.write(i);
            clientOutput.flush();
        }
    }

}
