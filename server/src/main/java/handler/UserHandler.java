package handler;

import io.javalin.http.Context;
import service.UserService;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {

    }

    public void login(Context ctx) {

    }

    public void logout(Context ctx) {

    }
}
