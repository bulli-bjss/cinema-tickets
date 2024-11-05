package uk.gov.dwp.uc.pairtest.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TicketTypeRequestHelperTest {

    TicketTypeRequestHelper ticketTypeRequestValidator = new TicketTypeRequestHelperImpl();

    @Test
    public void CanHaveValidRequestTypesRegardlessOfTicketNumbers() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 21);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 21);
        assertDoesNotThrow(() ->
                ticketTypeRequestValidator.validateTicketTypes(adultRequest, childRequest, infantRequest)
        );
    }

    @Test
    public void CanSubmitMaximumNumberOfTicketsInARequest() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        assertDoesNotThrow(() ->
                ticketTypeRequestValidator.getTicketRequestCounts(adultRequest)
        );
    }

    @Test
    public void CanSubmitMaximumTicketsInMultipleRequests() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        assertDoesNotThrow(() ->
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequest, childRequest, infantRequest)
        );
    }

    @Test
    public void CanNotHaveNullRequestType() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(null, 1);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.validateTicketTypes(adultRequest, adultRequest2);
        });
        assertTrue(exception.getMessage().contains("Ticket request contains null TicketType"));
    }

    @Test
    public void CanNotSubmitZeroTicketRequest() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 0);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 0);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequest, childRequest, infantRequest);
        });
        assertTrue(exception.getMessage().contains("Number of tickets requested is 0, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotSubmitNegativeTicketRequest() {
         TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequest);
        });
        assertTrue(exception.getMessage().contains("Number of tickets requested is -1, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotSubmitIndividualHighTicketNumbersToRolloverTwosCompliment() {
        //Integer.MAX_VALUE + Integer.MAX_VALUE = -2
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, Integer.MAX_VALUE);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, Integer.MAX_VALUE);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequest1, adultRequest2);
        });
        assertTrue(exception.getMessage().contains("Number of tickets requested is 2,147,483,647, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotBookMoreThan20AdultTickets() {
        TicketTypeRequest adultRequests = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequests);
        });
        assertTrue(exception.getMessage().contains("Number of tickets requested is 21, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotBookMoreThan20AdultAndChildTickets() {
        TicketTypeRequest adultRequests = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest childRequests = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 11);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequests, childRequests);
        });
        assertTrue(exception.getMessage().contains("Total number of tickets requested is 21, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotBookMoreThan20AdultAndInfantTickets() {
        TicketTypeRequest adultRequests = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest infantRequests = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 11);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(adultRequests, infantRequests);
        });
        assertTrue(exception.getMessage().contains("Total number of tickets requested is 21, but needs to be between 1 and 20."));
    }

    @Test
    public void CanNotBookSingleChildTicketWithoutAdultTicket() {
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(childRequest);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookMultipleChildTicketWithoutAdultTicket() {
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(infantRequest);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookMultipleRequestsOfChildTicketWithoutAdultTicket() {
        TicketTypeRequest childRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest childRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(childRequest1, childRequest2);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookSingleInfantTicketWithoutAdultTicket() {
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(infantRequest);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookMultipleInfantTicketsWithoutAdultTicket() {
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(infantRequest);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookMultipleRequestsOfInfantTicketsWithoutAdultTicket() {
        TicketTypeRequest childRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest childRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(childRequest1, childRequest2);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookChildAndInfantTicketWithoutAdultTicket() {
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(childRequest, infantRequest);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

    @Test
    public void CanNotBookMultipleRequestsOfChildAndInfantTicketWithoutAdultTicket() {
        TicketTypeRequest childRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infantRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        TicketTypeRequest childRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infantRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketTypeRequestValidator.getTicketRequestCounts(childRequest1, infantRequest1, childRequest2, infantRequest2);
        });
        assertTrue(exception.getMessage().contains("TicketTypeRequest contains 0 adult tickets, requires at least 1."));
    }

}
