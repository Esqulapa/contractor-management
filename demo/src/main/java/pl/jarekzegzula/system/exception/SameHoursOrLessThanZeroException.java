package pl.jarekzegzula.system.exception;

public class SameHoursOrLessThanZeroException extends RuntimeException {

  public SameHoursOrLessThanZeroException(String msg) {
    super(msg);
  }
}
