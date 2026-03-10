package dataaccess;

import model.UserData;

public class SQLUserDAO implements UserDAO{

    String[] createStatements = {
            """
            """
    };

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }
    
    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void clear() {

    }
}
