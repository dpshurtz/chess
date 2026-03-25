package client;

import com.google.gson.Gson;
import exception.ResponseException;
import serviceobjects.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public void clear()
            throws ResponseException {
        var request = buildRequest("DELETE", "/db", null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public RegisterResult register(RegisterRequest registerRequest)
            throws ResponseException {
        var request = buildRequest("POST", "/user", registerRequest);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public LoginResult login(LoginRequest loginRequest)
            throws ResponseException {
        var request = buildRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public void logout(LogoutRequest logoutRequest, String authToken)
            throws ResponseException {
        var request = buildRequest("DELETE", "/session", logoutRequest, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest, String authToken)
            throws ResponseException {
        var request = buildRequest("GET", "/game", listGamesRequest, authToken);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest, String authToken)
            throws ResponseException {
        var request = buildRequest("POST", "/game", createGameRequest, authToken);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResult.class);
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken)
            throws ResponseException {
        var request = buildRequest("PUT", "/game", joinGameRequest, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body))
                .header("Authorization", authToken);
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }
            throw new ResponseException("other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
