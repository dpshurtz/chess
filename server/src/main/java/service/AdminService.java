package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class AdminService {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    public void clear() {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
