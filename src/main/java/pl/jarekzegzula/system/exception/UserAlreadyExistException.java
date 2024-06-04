package pl.jarekzegzula.system.exception;

import jakarta.security.auth.message.AuthException;

public class UserAlreadyExistException extends AuthException {

  public UserAlreadyExistException(String msg) {
    super(msg);
  }
}
