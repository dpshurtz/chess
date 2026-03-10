package dataaccess;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO{

    String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                `id` int NOT NULL AUTO_INCREMENT,
                `authToken` varchar(256) NOT NULL,
                `username` varchar(256) NOT NULL,
                PRIMARY KEY (`id`),
                INDEX(authToken)
            )
            """
    };

    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
