package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthResult register(UserData request) throws DataAccessException {
        if (request == null || request.username() == null ||
                request.password() == null || request.email() == null) {
            throw new BadRequestException("bad request");
        }
        String username = request.username();

        UserData existingUser = dataAccess.getUser(username);
        if (existingUser != null) {
            throw new ForbiddenException("already taken");
        }
        // create the user
        dataAccess.createUser(request);
        // make a new auth token for this user
        AuthData authData = dataAccess.createAuth(username);
        return new AuthResult(authData.username(), authData.authToken(), null);
    }

    public AuthResult login(UserData request) throws DataAccessException {
        if (request == null || request.username() == null ||
                request.password() == null) {
            throw new BadRequestException("bad request");
        }
        String username = request.username();
        String password = request.password();

        UserData stored = dataAccess.getUser(username);
        if (stored == null) {
            throw new UnauthorizedException("unauthorized");
        }

        String correctPassword = stored.password();
        if (!correctPassword.equals(password)) {
            throw new UnauthorizedException("unauthorized");
        }

        AuthData auth = dataAccess.createAuth(username);
        return new AuthResult(auth.username(), auth.authToken(), null);
    }

    public void logout(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("unauthorized");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }
}
