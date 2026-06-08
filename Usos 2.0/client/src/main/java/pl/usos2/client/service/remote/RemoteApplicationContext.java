package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.service.audit.AuditLogService;

public class RemoteApplicationContext extends ApplicationContext {
    public RemoteApplicationContext(ApiClient apiClient, ClientSession session) {
        super(
                new RemoteAuthService(apiClient, session),
                AuditLogService.getInstance(),
                new RemoteGradeService(apiClient, session),
                new RemoteMessageService(apiClient, session),
                new RemoteRequestService(apiClient, session),
                new RemotePaymentService(apiClient, session),
                new RemoteEmployeeService(apiClient, session),
                new RemoteServiceTicketService(apiClient, session),
                new RemoteRentalService(apiClient, session),
                new RemoteCourseService(apiClient, session)
        );
    }
}
