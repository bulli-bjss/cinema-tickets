package uk.gov.dwp.uc.pairtest.exception;

import java.text.MessageFormat;

public class InvalidPurchaseException extends RuntimeException {

    public InvalidPurchaseException(String message, Object... values) {
        super(new MessageFormat(message).format(values));
    }

}
