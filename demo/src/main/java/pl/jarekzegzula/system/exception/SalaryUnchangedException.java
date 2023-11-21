package pl.jarekzegzula.system.exception;

public class SalaryUnchangedException extends RuntimeException{

    public SalaryUnchangedException(String message) {
        super(message);
    }
}
