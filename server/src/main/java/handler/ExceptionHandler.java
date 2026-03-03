package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.*;

import java.util.Map;

public class ExceptionHandler {
    private final Gson serializer = new Gson();

    public void httpExceptionHandler(HttpResponseException ex, Context ctx) {
        switch (ex) {
            case BadRequestResponse ignored     -> ctx.status(400).result("Error: bad request");
            case UnauthorizedResponse ignored   -> ctx.status(401).result("Error: unauthorized");
            case ForbiddenResponse ignored      -> ctx.status(403).result("Error: already taken");
            default                             -> ctx.status(500).result("Error: unknown");
        }
        ctx.result(toJson(ex));
    }

    public void dataExceptionHandler(DataAccessException ex, Context ctx) {
        ctx.status(500);
        ctx.result(toJson(ex));
    }

    public String toJson(Exception ex) {
        return serializer.toJson(Map.of("message", ex.getMessage()));
    }
}
