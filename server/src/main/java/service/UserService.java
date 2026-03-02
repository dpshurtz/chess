package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import model.UserData;
import serviceobjects.*;

import java.util.Objects;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest)
            throws ForbiddenResponse, DataAccessException {
        UserData user = userDAO.getUser(registerRequest.username());
        if (user != null) {
            throw new ForbiddenResponse("already taken");
        }
        userDAO.createUser(new UserData(
                registerRequest.username(),
                registerRequest.password(),
                registerRequest.email()));

        String authToken = AdminService.generateToken();
        authDAO.createAuth(new AuthData(authToken, registerRequest.username()));

        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest)
            throws UnauthorizedResponse, DataAccessException {
        UserData user = userDAO.getUser(loginRequest.username());
        if (user == null) {
            throw new UnauthorizedResponse("Username not found");
        }
        else if (!Objects.equals(user.password(), loginRequest.password())) {
            throw new UnauthorizedResponse("Incorrect password");
        }

        String authToken = AdminService.generateToken();
        authDAO.createAuth(new AuthData(authToken, loginRequest.username()));

        return new LoginResult(loginRequest.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest, String authToken)
            throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }
}
