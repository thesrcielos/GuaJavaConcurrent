package org.eci.arep;

import org.eci.arep.annotations.GetMapping;
import org.eci.arep.annotations.RequestParam;
import org.eci.arep.annotations.RestController;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
    private static final Map<String, Method> services = new HashMap<>();
    private static final Map<String, List<Parameter>> parameters = new HashMap<>();
    
    private static String WEB_ROOT_DIR = "public";

    public static void loadComponents(String[] args){
        try {
            List<Class<?>> classes = ComponentScanner.scanForControllers("org.eci.arep");
            for (Class<?> cl : classes){
                loadComponent(cl);
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadComponent(Class<?> c){
        if(!c.isAnnotationPresent(RestController.class)) {
            return;
        }
        Method[] methods = c.getDeclaredMethods();
        for(Method m : methods){
            if(!m.isAnnotationPresent(GetMapping.class)){
                continue;
            }
            String mapping = m.getAnnotation(GetMapping.class).value();
            System.out.println(mapping);
            services.put(mapping, m);
            checkMethodParameters(m, mapping);
        }
    }

    private static void checkMethodParameters(Method method, String mapping){
        Parameter[] params = method.getParameters();
        for(Parameter p : params){
            if(p.isAnnotationPresent(RequestParam.class)){
                parameters.computeIfAbsent(mapping, l -> new ArrayList<>()).add(p);
            }
        }
    }
    public static void get(String path, Method service){
        services.put(path, service);
    }

    
    public static void staticfiles(String path){
        WEB_ROOT_DIR = path;
    }

    private static int getPort(){
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Variable PORT is not a number using default value 35000.");
            }
        }
        return 35000;
    }

    public static void run(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            serverSocket = new ServerSocket(getPort());
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        loadComponents(args);
        boolean running = true;
        while(running){
            try {
                System.out.println("Listo para recibir...");
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
            } catch (IOException e) {
                Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, e);
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException, URISyntaxException  {
       run(args);
    }

    private static HttpRequest getClientHttpRequest(BufferedReader in) throws IOException, URISyntaxException {
        String inputLine;
        boolean firstLine = true;
        HttpRequest request = new HttpRequest();
        URI requestUri = null;

        while ((inputLine = in.readLine()) != null) {
            if (firstLine) {
                String[] parts = inputLine.split(" ");
                requestUri = new URI(parts[1]);
                request.setMethod(parts[0]);
                request.setUri(requestUri);
                request.setHttpVersion(parts[2]);
                firstLine = false;
            } else {
                String[] headerParts = inputLine.split(":", 2);
                if (headerParts.length == 2) {
                    request.addHeader(headerParts[0].trim(), headerParts[1].trim());
                }
            }
            if (!in.ready()) {
                break;
            }
        }
        return request;
    }

    private static void handleClient(Socket clientSocket) throws IOException, URISyntaxException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        HttpRequest request = getClientHttpRequest(in);
        try {
            handleHttpRequest(request, out, clientSocket);
        } catch (Exception ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        out.close();
        in.close();
        clientSocket.close();
    }


    public static void handleDynamicRequest(Socket clientSocket, HttpRequest request) throws IOException, IllegalAccessException, InvocationTargetException {
        HttpResponse response = new HttpResponse();
        URI requestUri = request.getUri();

        Method handler = services.get(requestUri.getPath());
        if (handler != null) {
            Object[] params = getRequestParamsValues(request);
            Object body = handler.invoke(null, params);
            response.setBody(body.toString());
        } else {
            response.setStatus(404, "Not Found");
            response.setContentType("text/plain; charset=utf-8");
            response.setBody("Not found: " + requestUri.getPath());
        }

        response.send(clientSocket.getOutputStream());
    }


    public static Object[] getRequestParamsValues(HttpRequest request){
        List<Parameter> parameterList = parameters.get(request.getUri().getPath());
        Object[] params = new Object[parameterList.size()];

        for(int i = 0; i < parameterList.size(); i++) {
            Parameter param = parameterList.get(i);
            if (param == null) {
                continue;
            }

            RequestParam values = param.getAnnotation(RequestParam.class);
            String rawValue = request.getValues(values.value());
            if (rawValue == null || rawValue.isEmpty()){
                if (!values.defaultValue().equals(RequestParam.DEFAULT_NONE)) {
                    rawValue = values.defaultValue();
                }
            }

            Class<?> type = param.getType();
            Object convertedValue = convertValue(rawValue, type);
            params[i] = convertedValue;
        }

        return params;
    }

    public static void handleHttpRequest(HttpRequest request, PrintWriter out, Socket clientSocket)
            throws IOException, InvocationTargetException, IllegalAccessException {
        URI requestUri = request.getUri();
        if (requestUri != null) {
            String resourcePath = WEB_ROOT_DIR + requestUri.getPath();
            if (resourcePath.endsWith("/")) {
                resourcePath += "index.html";
            }

            InputStream resourceStream = Main.class.getClassLoader().getResourceAsStream(resourcePath);

            if (resourceStream != null) {
                String contentType = getContentType(resourcePath);

                byte[] fileBytes = resourceStream.readAllBytes();
                String outputLine = "HTTP/1.1 200 OK\r\n"
                        + "content-type: " + contentType + "\r\n"
                        + "content-length: " + fileBytes.length + "\r\n"
                        + "\r\n";

                try (OutputStream outputStream = clientSocket.getOutputStream()) {
                    outputStream.write(outputLine.getBytes());
                    outputStream.write(fileBytes);
                }
            } else {
                handleDynamicRequest(clientSocket, request);
            }
        }
    }


    private static Object convertValue(String value, Class<?> type) {
        if (value == null) return null;

        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value.toUpperCase());
        }

        return value;
    }

    public static String getContentType(String path) {
        String name = path.toLowerCase(Locale.ROOT);
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif"))  return "image/gif";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        if (name.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}
