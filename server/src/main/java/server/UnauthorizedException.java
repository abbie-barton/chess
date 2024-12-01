package server;

public class UnauthorizedException extends Exception {

  public UnauthorizedException(int statusCode, String message) {
    super(message);
  }

}
