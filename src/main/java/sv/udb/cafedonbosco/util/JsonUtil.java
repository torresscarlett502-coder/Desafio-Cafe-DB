package sv.udb.cafedonbosco.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sv.udb.cafedonbosco.dto.response.ApiResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public final class JsonUtil {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(FORMATO_FECHA)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), FORMATO_FECHA))
            .create();

    private JsonUtil() {
    }

    public static <T> T leerCuerpo(HttpServletRequest request, Class<T> clase) throws IOException {
        StringBuilder cuerpo = new StringBuilder();
        try (BufferedReader lector = request.getReader()) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                cuerpo.append(linea);
            }
        }
        if (cuerpo.length() == 0) {
            return null;
        }
        return GSON.fromJson(cuerpo.toString(), clase);
    }

    public static void escribirJson(HttpServletResponse response, int codigoHttp, Object datos) throws IOException {
        response.setStatus(codigoHttp);
        response.setContentType(Constantes.CONTENT_TYPE_JSON);
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter escritor = response.getWriter()) {
            escritor.write(GSON.toJson(datos));
        }
    }

    public static void exito(HttpServletResponse response, int codigoHttp, String mensaje, Object datos) throws IOException {
        escribirJson(response, codigoHttp, new ApiResponse<>(true, mensaje, datos));
    }

    public static void error(HttpServletResponse response, int codigoHttp, String mensaje) throws IOException {
        escribirJson(response, codigoHttp, new ApiResponse<>(false, mensaje, null));
    }
}
