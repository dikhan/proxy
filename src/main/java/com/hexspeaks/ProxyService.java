package com.hexspeaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProxyService {

    private final static Logger logger = LoggerFactory.getLogger(ProxyService.class);

    private static final int MAX_NUMBER_CLIENT_THREADS = 10;

    private final Executor executor;
    private final int serverPort;

    public ProxyService(int serverPort) {
        this.serverPort = serverPort;
        this.executor = Executors.newFixedThreadPool(MAX_NUMBER_CLIENT_THREADS);
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        logger.info("Proxy service listening on " + serverSocket);
        while(true) {
            Socket clientSocket = serverSocket.accept();
            executor.execute(new ClientTask(clientSocket));
        }
    }

    public static void main(String[] args) throws IOException {
        ProxyService proxyService = new ProxyService(8080);
        proxyService.start();
    }
}
