package dataaccess;

import model.UserData;

public class SQLUserDAO implements UserDAO{

    String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
                `id` int NOT NULL AUTO_INCREMENT,
                `username` varchar(256) NOT NULL,
                `password` varchar(256) NOT NULL,
                `email` varchar(256) NOT NULL,
                PRIMARY KEY (`id`),
                INDEX(username)
            )
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
