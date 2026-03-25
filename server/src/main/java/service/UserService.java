package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;
import model.UserData;
import model.AuthData;

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

        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(username, hashedPassword, request.email());

        dataAccess.createUser(hashedUser);
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

        // Compare against the stored BCrypt hash
        if (!BCrypt.checkpw(password, stored.password())) {
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