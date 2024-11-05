package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.helpers.TicketTypeRequestHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    static TicketPriceService ticketPriceService;

    @Mock
    TicketPaymentService ticketPaymentService;

    @Mock
    SeatReservationService seatReservationService;

    @Mock
    TicketTypeRequestHelper ticketTypeRequestHelper;

    @InjectMocks
    TicketService ticketService = new TicketServiceImpl();


    private static final Map<TicketTypeRequest.Type, Integer> prices;
    static {
        prices = Map.of(TicketTypeRequest.Type.ADULT,20,
                TicketTypeRequest.Type.CHILD, 10,
                TicketTypeRequest.Type.INFANT, 0);
    }

    private void mockTicketPrices(TicketTypeRequest.Type... types) {
        for (TicketTypeRequest.Type type : types) {
            doReturn(prices.get(type)).when(ticketPriceService).getTicketPrice(type);
        }
    }


    // ####################  Happy day scenarios
    @Test
    public void BookingSingleAdultTicketReservesSeatAndPaysForTicket() {
        Long accountId = 100L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 1);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT);

        ticketService.purchaseTickets(accountId, adultRequest);
        verify(ticketPaymentService, times(1)).makePayment(100L , 20);
        verify(seatReservationService, times(1)).reserveSeat(100L , 1);
    }

    @Test
    public void BookingMaximumAdultTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 101L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 20);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT);

        ticketService.purchaseTickets(accountId, adultRequest);
        verify(ticketPaymentService, times(1)).makePayment(101L , 400);
        verify(seatReservationService, times(1)).reserveSeat(101L , 20);
    }

    @Test
    public void BookingMultipleRequestsOfAdultTicketReservesSeatsAndPaysForTickets() {
        Long accountId = 102L;
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 4);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest1, adultRequest2);
        mockTicketPrices(TicketTypeRequest.Type.ADULT);

        ticketService.purchaseTickets(accountId, adultRequest1, adultRequest2);
        verify(ticketPaymentService, times(1)).makePayment(102L , 80);
        verify(seatReservationService, times(1)).reserveSeat(102L , 4);
    }

    @Test
    public void BookingSingleAdultAndChildTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 103L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 1);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 1);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, childRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest);
        verify(ticketPaymentService, times(1)).makePayment(103L , 30);
        verify(seatReservationService, times(1)).reserveSeat(103L , 2);
    }

    @Test
    public void BookingMaximumAdultAndChildTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 104L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 10);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 10);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, childRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest);
        verify(ticketPaymentService, times(1)).makePayment(104L , 300);
        verify(seatReservationService, times(1)).reserveSeat(104L , 20);
    }

    @Test
    public void BookingMultipleAdultAndChildTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 105L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 2);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 2);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, childRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest);
        verify(ticketPaymentService, times(1)).makePayment(105L , 60);
        verify(seatReservationService, times(1)).reserveSeat(105L , 4);
    }

    @Test
    public void BookingMultipleRequestsOfAdultAndChildTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 106L;
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest childRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 4);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 4);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest1, adultRequest2, childRequest1, childRequest2);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD);

        ticketService.purchaseTickets(accountId, adultRequest1, adultRequest2, childRequest1, childRequest2);
        verify(ticketPaymentService, times(1)).makePayment(106L , 120);
        verify(seatReservationService, times(1)).reserveSeat(106L , 8);
    }

    @Test
    public void BookingSingleAdultAndInfantTicketsReservesAdultSeatAndPaysForOnlyAdultTicket() {
        Long accountId = 107L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 1);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 1);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, infantRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest, infantRequest);
        verify(ticketPaymentService, times(1)).makePayment(107L , 20);
        verify(seatReservationService, times(1)).reserveSeat(107L , 1);
    }


    @Test
    public void BookingMaximumAdultAndInfantTicketsReservesSeatsAndPaysForTickets() {
        Long accountId = 108L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 10);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 10);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, infantRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest, infantRequest);
        verify(ticketPaymentService, times(1)).makePayment(108L , 200);
        verify(seatReservationService, times(1)).reserveSeat(108L , 10);
    }

    @Test
    public void BookingMultipleRequestsOfAdultAndInfantTicketsReservesAdultsSeatsAndPaysForAdultTickets() {
        Long accountId = 109L;
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest infantRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        TicketTypeRequest infantRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 4);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 4);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest1, adultRequest2, infantRequest1, infantRequest2);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest1, adultRequest2, infantRequest1, infantRequest2);
        verify(ticketPaymentService, times(1)).makePayment(109L , 80);
        verify(seatReservationService, times(1)).reserveSeat(109L , 4);
    }


    @Test
    public void BookingSingleAdultAndChildAndInfantTicketsReservesSeatsAndPaysForCorrectTickets() {
        Long accountId = 110L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 1);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 1);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 1);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, childRequest, infantRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        verify(ticketPaymentService, times(1)).makePayment(110L , 30);
        verify(seatReservationService, times(1)).reserveSeat(110L , 2);
    }


    @Test
    public void BookingMaximumAdultAndChildAndInfantTicketsReservesAndPaysForChildAndAdultSeats() {
        Long accountId = 111L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 5);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 10);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 5);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest, childRequest, infantRequest);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        verify(ticketPaymentService, times(1)).makePayment(111L , 200);
        verify(seatReservationService, times(1)).reserveSeat(111L , 15);
    }


    @Test
    public void BookingMultipleRequestsOfAdultAndChildAndInfantTicketsReservesAndPaysForAdultAndChildSeats() {
        Long accountId = 112L;
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest childRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infantRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        TicketTypeRequest infantRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        Map<TicketTypeRequest.Type, Integer> ticketCounts = new HashMap<>();
        ticketCounts.put(TicketTypeRequest.Type.ADULT, 4);
        ticketCounts.put(TicketTypeRequest.Type.CHILD, 4);
        ticketCounts.put(TicketTypeRequest.Type.INFANT, 4);
        doReturn(ticketCounts).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest1, adultRequest2,
                childRequest1, childRequest2, infantRequest1, infantRequest2);
        mockTicketPrices(TicketTypeRequest.Type.ADULT, TicketTypeRequest.Type.CHILD, TicketTypeRequest.Type.INFANT);

        ticketService.purchaseTickets(accountId, adultRequest1, adultRequest2, childRequest1, childRequest2, infantRequest1, infantRequest2);
        verify(ticketPaymentService, times(1)).makePayment(112L , 120);
        verify(seatReservationService, times(1)).reserveSeat(112L , 8);
    }


    // ####################  Unhappy day scenarios
    @Test
    public void CannotHaveNullAccountId() {
        Long accountId = null;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, adultRequest);
        });
        assertTrue(exception.getMessage().contains("Account id null is invalid."));
        verify(ticketPaymentService, never()).makePayment(anyLong() , anyInt());
        verify(seatReservationService, never()).reserveSeat(anyLong() , anyInt());
    }

    @Test
    public void CannotBookWithAccountIdLessThan1() {
        Long accountId = 0L;
        TicketTypeRequest adultRequests = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, adultRequests);
        });
        assertTrue(exception.getMessage().contains("Account id 0 is invalid."));
        verify(ticketPaymentService, never()).makePayment(anyLong() , anyInt());
        verify(seatReservationService, never()).reserveSeat(anyLong() , anyInt());
    }

    @Test
    public void TicketTypesExceptionPreventsSeatReservationAndPayment() {
        Long accountId = 113L;
        TicketTypeRequest adultRequest1 = new TicketTypeRequest(null, 1);
        TicketTypeRequest adultRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        doThrow(InvalidPurchaseException.class).when(ticketTypeRequestHelper).validateTicketTypes(adultRequest1, adultRequest2);
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, adultRequest1, adultRequest2);
        });
        verify(ticketPaymentService, never()).makePayment(anyLong() , anyInt());
        verify(seatReservationService, never()).reserveSeat(anyLong() , anyInt());
    }

    @Test
    public void TicketCountExceptionPreventsSeatReservationAndPayment() {
        Long accountId = 114L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        doThrow(InvalidPurchaseException.class).when(ticketTypeRequestHelper).getTicketRequestCounts(adultRequest);
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, adultRequest);
        });
        verify(ticketPaymentService, never()).makePayment(anyLong() , anyInt());
        verify(seatReservationService, never()).reserveSeat(anyLong() , anyInt());
    }

}
