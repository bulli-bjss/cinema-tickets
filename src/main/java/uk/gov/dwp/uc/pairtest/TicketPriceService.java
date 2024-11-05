package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

// Assuming there would be some sort of service to retrieve ticket prices.
public interface TicketPriceService {

    int getTicketPrice(TicketTypeRequest.Type type);

}
