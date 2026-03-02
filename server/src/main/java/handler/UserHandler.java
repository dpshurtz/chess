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

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) throws ForbiddenResponse, DataAccessException {
        RegisterRequest registerRequest = new Gson().fromJson(ctx.body(), RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);
        ctx.result(new Gson().toJson(registerResult));
        ctx.status(200);
    }

    public void login(Context ctx) throws UnauthorizedResponse, DataAccessException {
        LoginRequest loginRequest = new Gson().fromJson(ctx.body(), LoginRequest.class);

        LoginResult loginResult = userService.login(loginRequest);
        ctx.result(new Gson().toJson(loginResult));
        ctx.status(200);
    }

    public void logout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("Authorization");

        userService.logout(new LogoutRequest(), authToken);
        ctx.status(200);
    }
}
