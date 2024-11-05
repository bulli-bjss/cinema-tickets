package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.helpers.TicketTypeRequestHelper;

import java.util.Map;

public class TicketServiceImpl implements TicketService {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketPriceService ticketPriceService;
    private TicketTypeRequestHelper ticketTypeRequestHelper;

    /**
     * Requests to reserve seats and pay for the tickets specified.
     * @param accountId the account to be used to pay for tickets
     * @param ticketTypeRequests an array of ticket requests that are to be purchased
     * @throws InvalidPurchaseException if there is an issue with the given accountId or ticketTypeRequest
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccountId(accountId);
        ticketTypeRequestHelper.validateTicketTypes(ticketTypeRequests);
        Map<TicketTypeRequest.Type, Integer> ticketCounts = ticketTypeRequestHelper.getTicketRequestCounts(ticketTypeRequests);
        ticketPaymentService.makePayment(accountId, calculateTicketRequestTotalPrice(ticketCounts));
        seatReservationService.reserveSeat(accountId, calculateTotalSeatsRequired(ticketCounts));
    }


    private void validateAccountId(Long accountId) {

        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account id {0} is invalid.", accountId);
        }
    }


    private int calculateTicketRequestTotalPrice(Map<TicketTypeRequest.Type, Integer> ticketCounts) {

        return ticketCounts.keySet().stream()
                .map(ticketType ->
                        ticketPriceService.getTicketPrice(ticketType) * ticketCounts.get(ticketType))
                .reduce(0, Integer::sum);
    }


    private int calculateTotalSeatsRequired(Map<TicketTypeRequest.Type, Integer> ticketCounts) {

        return ticketCounts.keySet().stream()
                .filter(tickets -> !tickets.name().equals(TicketTypeRequest.Type.INFANT.name()))
                .map(ticketCounts::get)
                .reduce(0, Integer::sum);
    }

}
