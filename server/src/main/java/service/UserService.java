package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import serviceobjects.*;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        return null;
    }

    public LoginResult login(LoginRequest loginRequest) {
        return null;
    }

    public void logout(LogoutRequest logoutRequest) {

    }
}
