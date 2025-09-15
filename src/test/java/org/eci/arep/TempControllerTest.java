package org.eci.arep;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TempControllerTest {

    @BeforeAll
    public static void setUp() {
        HttpServer.loadComponents(new String[]{});
    }

    @Test
    public void testTempEndpoint_withParam() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUri()).thenReturn(new URI("/temp"));
        when(request.getValues("msg")).thenReturn("Hola");

        Socket socket = mock(Socket.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(baos);

        HttpServer.handleDynamicRequest(socket, request);

        String responseText = baos.toString();

        assertTrue(responseText.contains("Temp says: Hola"));
    }

    @Test
    public void testTempEndpoint_withDefaultValue() throws Exception {
        // Mock request sin query param
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUri()).thenReturn(new URI("/temp"));
        when(request.getValues("msg")).thenReturn(null);

        // Mock socket y output
        Socket socket = mock(Socket.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(baos);

        // Ejecutar dispatcher
        HttpServer.handleDynamicRequest(socket, request);

        String responseText = baos.toString();

        // Verificar que usa el defaultValue
        assertTrue(responseText.contains("Temp says: default"));
    }
}
