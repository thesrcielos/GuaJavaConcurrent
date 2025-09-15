package org.eci.arep;

import java.io.OutputStream;
import java.io.PrintWriter;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private String contentType = "text/plain";
    private String body = "";

    public void setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void send(OutputStream output) {
        PrintWriter writer = new PrintWriter(output, true);
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        writer.printf("Content-Type: %s\r\n", contentType);
        writer.printf("Content-Length: %d\r\n", body.getBytes().length);
        writer.print("\r\n");
        writer.print(body);
        writer.flush();
    }
}
