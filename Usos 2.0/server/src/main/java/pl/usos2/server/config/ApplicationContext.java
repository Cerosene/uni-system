package pl.usos2.server.config;

import pl.usos2.server.service.admin.EmployeeService;
import pl.usos2.server.service.auth.AuthService;
import pl.usos2.server.service.course.CourseService;
import pl.usos2.server.service.finance.PaymentService;
import pl.usos2.server.service.grade.GradeService;
import pl.usos2.server.service.maintenance.ServiceTicketService;
import pl.usos2.server.service.message.MessageService;
import pl.usos2.server.service.rental.RentalService;
import pl.usos2.server.service.request.RequestService;

public class ApplicationContext {

    private final AuthService authService = new AuthService();
    private final GradeService gradeService = new GradeService();
    private final MessageService messageService = new MessageService();
    private final RequestService requestService = new RequestService();
    private final PaymentService paymentService = new PaymentService();
    private final EmployeeService employeeService = new EmployeeService();
    private final ServiceTicketService serviceTicketService = new ServiceTicketService();
    private final RentalService rentalService = new RentalService();
    private final CourseService courseService = new CourseService();

    public AuthService getAuthService() {
        return authService;
    }

    public GradeService getGradeService() {
        return gradeService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public RequestService getRequestService() {
        return requestService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public EmployeeService getEmployeeService() {
        return employeeService;
    }

    public ServiceTicketService getServiceTicketService() {
        return serviceTicketService;
    }

    public CourseService getCourseService() {
        return courseService;
    }

    public RentalService getRentalService() {
        return rentalService;
    }
}
