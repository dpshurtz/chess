package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import model.UserData;
import serviceobjects.*;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest)
            throws ForbiddenResponse, DataAccessException, BadRequestResponse {
        if (
                registerRequest.username() == null ||
                registerRequest.password() == null ||
                registerRequest.email() == null
        ) {
            throw new BadRequestResponse("Error: bad request");
        }

        UserData user = userDAO.getUser(registerRequest.username());
        if (user != null) {
            throw new ForbiddenResponse("Error: already taken");
        }
        userDAO.createUser(new UserData(
                registerRequest.username(),
                registerRequest.password(),
                registerRequest.email()));

        String authToken = generateToken();
        authDAO.createAuth(new AuthData(authToken, registerRequest.username()));

        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest)
            throws UnauthorizedResponse, DataAccessException {
        if (
                loginRequest.username() == null ||
                loginRequest.password() == null
        ) {
            throw new BadRequestResponse("Error: bad request");
        }

        UserData user = userDAO.getUser(loginRequest.username());
        if (user == null) {
            throw new UnauthorizedResponse("Error: unauthorized");
        }
        else if (!Objects.equals(user.password(), loginRequest.password())) {
            throw new UnauthorizedResponse("Error: unauthorized");
        }

        String authToken = generateToken();
        authDAO.createAuth(new AuthData(authToken, loginRequest.username()));

        return new LoginResult(loginRequest.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest, String authToken)
            throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedResponse("Error: unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
