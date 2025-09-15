package org.eci.arep;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.eci.arep.HttpServer.get;
import static org.eci.arep.HttpServer.staticfiles;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        HttpServer.run(args);
    }

}
