
package com.example.literalura.cliente;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class GutendexClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl = "https://gutendex.com/books";

    public GutendexClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL) // 👈 Esto sigue redirecciones 301, 302, 307...
                .connectTimeout(Duration.ofSeconds(60))
                .build();
        ;

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Busca libros y devuelve el JSON puro.
     */
    public String buscarLibros(String query) throws IOException, InterruptedException {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String uri = baseUrl + "?search=" + q;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("⚠ Error HTTP " + response.statusCode() + " al consultar Gutendex");
            return null;
        }

        String body = response.body();
        if (body == null || body.isBlank()) {
            System.out.println("⚠ La respuesta de Gutendex está vacía.");
            return null;
        }

        return body;
    }

    /**
     * Convierte el JSON en un objeto GutendexResponse.
     */
    public GutendexResponse buscarLibro(String query) throws IOException, InterruptedException {
        String json = buscarLibros(query);
        if (json == null) {
            return new GutendexResponse(); // evita null y devuelve objeto vacío
        }
        return objectMapper.readValue(json, GutendexResponse.class);
    }

    /**
     * Imprime la lista de libros encontrados.
     */
    public void imprimirLibros(String query) throws IOException, InterruptedException {
        GutendexResponse response = buscarLibro(query);
        if (response.getResults() == null || response.getResults().isEmpty()) {
            System.out.println("⚠ No se encontraron resultados para: " + query);
            return;
        }
        for (GutendexBook libro : response.getResults()) {
            System.out.println(libro);
        }
    }
}
