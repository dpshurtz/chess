package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO{
    private Collection<UserData> userTable = new ArrayList<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userTable.contains(user)) {
            throw new DataAccessException("Error: User already exists");
        }
        userTable.add(user);
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : userTable) {
            if (Objects.equals(username, user.username())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        userTable = new ArrayList<>();
    }

    public Collection<UserData> getUserTable() {
        return userTable;
    }
}
