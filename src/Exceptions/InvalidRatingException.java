package Exceptions;

public class InvalidRatingException extends Exception {
    public InvalidRatingException(String message) {
        super(message);
    }
}