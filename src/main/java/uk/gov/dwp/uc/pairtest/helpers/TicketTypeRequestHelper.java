package uk.gov.dwp.uc.pairtest.helpers;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Map;

public interface TicketTypeRequestHelper {

    /**
     * Checks to see if any ticketTypeRequest types are invalid
     * @param ticketTypeRequests The array of TicketTypeRequests to validate
     * @throws InvalidPurchaseException if any of the ticketTypeRequests are invalid
     */
    void validateTicketTypes(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException;

    /**
     * Retrieves TicketTypeRequest counts.
     * @param ticketTypeRequests: The array of TicketTypeRequests to validate the requested ticket counts
     * @return A map of TicketTypeRequest.Type to a count of tickets with each type, in the ticketTypeRequests.
     * @throws InvalidPurchaseException if the ticketTypeRequests are invalid.
     */
    Map<TicketTypeRequest.Type, Integer> getTicketRequestCounts(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException;

}
