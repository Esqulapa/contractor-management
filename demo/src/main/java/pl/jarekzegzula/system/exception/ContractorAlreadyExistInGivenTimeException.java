package pl.jarekzegzula.system.exception;

public class ContractorAlreadyExistInGivenTimeException extends RuntimeException {
  public ContractorAlreadyExistInGivenTimeException(String message) {
    super(message);
  }
}
