package Exceptions;

public class CSVFormatException extends RuntimeException {
    public CSVFormatException(String message) {
        super(message);
    }
}
