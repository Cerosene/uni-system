package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.User;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.rental.RentalService;

import java.time.LocalDate;
import java.util.List;

public class RemoteRentalService extends RentalService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteRentalService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Rental createRental(Long id, User borrower, String resourceName, LocalDate rentalDate, LocalDate returnDate) {
        return (Rental) apiClient.send(ApiAction.RENTAL_CREATE, session.getToken(), apiClient.payload(
                "rentalId", id, "borrower", borrower, "resourceName", resourceName, "rentalDate", rentalDate, "returnDate", returnDate
        ));
    }

    @Override
    public Rental returnRental(Long rentalId) {
        return (Rental) apiClient.send(ApiAction.RENTAL_RETURN, session.getToken(), apiClient.payload("rentalId", rentalId));
    }

    @Override
    public Rental extendReturnDate(Long rentalId, LocalDate newReturnDate) {
        return (Rental) apiClient.send(ApiAction.RENTAL_EXTEND, session.getToken(), apiClient.payload("rentalId", rentalId, "returnDate", newReturnDate));
    }

    @Override
    public Rental findById(Long rentalId) {
        return (Rental) apiClient.send(ApiAction.RENTAL_FIND_BY_ID, session.getToken(), apiClient.payload("rentalId", rentalId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getRentalsForBorrower(User borrower) {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_BORROWER, session.getToken(), apiClient.payload("borrower", borrower));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getRentalsForResource(String resourceName) {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_RESOURCE, session.getToken(), apiClient.payload("resourceName", resourceName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getActiveRentals() {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_ACTIVE, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getReturnedRentals() {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_RETURNED, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getOverdueRentals(LocalDate currentDate) {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_OVERDUE, session.getToken(), apiClient.payload("currentDate", currentDate));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rental> getAllRentals() {
        return (List<Rental>) apiClient.send(ApiAction.RENTAL_LIST_ALL, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAvailableResources() {
        return (List<String>) apiClient.send(ApiAction.RENTAL_LIST_AVAILABLE_RESOURCES, session.getToken());
    }
}
