package com.github.dikhan.http;

public enum HttpMethod {

    // The OPTIONS method returns the HTTP methods that the server supports for the specified URL.
    OPTIONS,

    // The GET method requests a representation of the specified resource.
    GET,

    // The HEAD method asks for a response identical to that of a GET request, but without the response body.
    HEAD,

    // The POST method requests that the server accept the entity enclosed in the request as a new subordinate of the
    // web resource identified by the URI.
    POST,

    // The PUT method requests that the enclosed entity be stored under the supplied URI.
    PUT,

    // The DELETE method deletes the specified resource.
    DELETE,

    // The TRACE method echoes the received request so that a client can see what (if any) changes or additions have
    // been made by intermediate servers.
    TRACE,

    // The CONNECT method converts the request connection to a transparent TCP/IP tunnel, usually to facilitate
    // SSL-encrypted communication (HTTPS) through an unencrypted HTTP proxy.[17][18] See HTTP CONNECT tunneling.
    CONNECT

}
