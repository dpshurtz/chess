package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryAuthDAO implements AuthDAO{
    private Collection<AuthData> authTable = new HashSet<>();

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        if (authTable.contains(authData)) {
            throw new DataAccessException("Error: AuthData already exists");
        }
        authTable.add(authData);
    }

    @Override
    public AuthData getAuth(String authToken) {
        for (AuthData authData : authTable) {
            if (Objects.equals(authToken, authData.authToken())) {
                return authData;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        for (AuthData authData : authTable) {
            if (Objects.equals(authToken, authData.authToken())) {
                authTable.remove(authData);
                return;
            }
        }
        throw new DataAccessException("Error: AuthToken not found");
    }

    @Override
    public void clear() {
        authTable = new HashSet<>();
    }

    public Collection<AuthData> getAuthTable() {
        return authTable;
    }
}
