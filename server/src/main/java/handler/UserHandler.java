package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import service.UserService;
import serviceobjects.*;

public class UserHandler {
    private final UserService userService;
    private final Gson serializer = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx)
            throws ForbiddenResponse, DataAccessException {
        RegisterRequest registerRequest = serializer.fromJson(ctx.body(), RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);
        ctx.result(serializer.toJson(registerResult));
        ctx.status(200);
    }

    public void login(Context ctx)
            throws UnauthorizedResponse, DataAccessException {
        LoginRequest loginRequest = serializer.fromJson(ctx.body(), LoginRequest.class);

        LoginResult loginResult = userService.login(loginRequest);
        ctx.result(serializer.toJson(loginResult));
        ctx.status(200);
    }

    public void logout(Context ctx)
            throws DataAccessException {
        String authToken = ctx.header("Authorization");
        LogoutRequest logoutRequest = new Gson().fromJson(ctx.body(), LogoutRequest.class);

        userService.logout(logoutRequest, authToken);
        ctx.status(200);
    }
}
