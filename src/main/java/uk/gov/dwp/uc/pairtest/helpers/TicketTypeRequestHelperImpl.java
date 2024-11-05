package uk.gov.dwp.uc.pairtest.helpers;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TicketTypeRequestHelperImpl implements TicketTypeRequestHelper {

    //Could be defined from config
    int MAX_TICKETS_PER_TRANSACTION = 20;
    int MIN_TICKETS_PER_TRANSACTION = 1;
    int MIN_ADULTS_PER_TRANSACTION = 1;


    /**
     * {@inheritDoc}
     * @throws InvalidPurchaseException if any of the ticketTypeRequests are null
     */
    public void validateTicketTypes(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        if ((int) Arrays.stream(ticketTypeRequests)
                .filter(request -> request.type() == null)
                .count() > 0) {
            throw new InvalidPurchaseException("Ticket request contains null TicketType");
        }
    }


    /**
     * {@inheritDoc}
     * Validates and retrieves TicketTypeRequest counts, to ensure they do not exceed the maximum allowed either individually or in total.
     * @throws InvalidPurchaseException if the requested number of any type or the total requested tickets exceeds the maximum.
     */
    public Map<TicketTypeRequest.Type, Integer> getTicketRequestCounts(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        Map<TicketTypeRequest.Type, Integer> ticketCounts = getCountsForAllTicketTypes(ticketTypeRequests);

        if (ticketCounts.get(TicketTypeRequest.Type.ADULT) < MIN_ADULTS_PER_TRANSACTION ) {
            throw new InvalidPurchaseException("TicketTypeRequest contains {0} adult tickets, requires at least {1}.",
                    ticketCounts.get(TicketTypeRequest.Type.ADULT), MIN_ADULTS_PER_TRANSACTION);
        }

        int totalTicketCount = ticketCounts.values()
                .stream()
                .reduce(0, Integer::sum);

        if (totalTicketCount < MIN_TICKETS_PER_TRANSACTION || totalTicketCount > MAX_TICKETS_PER_TRANSACTION) {
            throw new InvalidPurchaseException("Total number of tickets requested is {0}, but needs to be between {1} and {2}.",
                    totalTicketCount, MIN_TICKETS_PER_TRANSACTION, MAX_TICKETS_PER_TRANSACTION);
        }

        return ticketCounts;
    }


    /**
     * Counts the number of each type of tickets in the given TicketTypeRequests
     * @param ticketTypeRequests: The array of TicketTypeRequests to search through
     * @return A map of TicketTypeRequest.Type to a count of tickets with each type, in the ticketTypeRequests.
     */
    private Map<TicketTypeRequest.Type, Integer> getCountsForAllTicketTypes(TicketTypeRequest... ticketTypeRequests) {

        return Stream.of(TicketTypeRequest.Type.values()).collect(
                Collectors.toMap(type -> type, type -> getCountForTicketType(type, ticketTypeRequests))
        );
    }


    /**
     * Counts the number of tickets in the given TicketTypeRequests for a given specific ticket type.
     * @param type: the TicketTypeRequest.Type to search for and count
     * @param ticketTypeRequests: the array of TicketTypeRequests to search through
     * @return a count of the number of tickets of the given type that are within the given array of TicketTypeRequests
     * @throws InvalidPurchaseException: if the number of tickets requested is less than minimum or more than maximum allowed.
     */
    private int getCountForTicketType(TicketTypeRequest.Type type, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        return Arrays.stream(ticketTypeRequests)
                .filter(d->d.type() == type)
                .map(ticketTypeRequest -> {
                    if (ticketTypeRequest.noOfTickets() >= MIN_TICKETS_PER_TRANSACTION &&
                            ticketTypeRequest.noOfTickets() <= MAX_TICKETS_PER_TRANSACTION) {
                        return ticketTypeRequest.noOfTickets();
                    } else {
                        throw new InvalidPurchaseException("Number of tickets requested is {0}, but needs to be between {1} and {2}.",
                                ticketTypeRequest.noOfTickets(), MIN_TICKETS_PER_TRANSACTION, MAX_TICKETS_PER_TRANSACTION);
                    }
                })
                .reduce(0, Integer::sum);
    }

}
