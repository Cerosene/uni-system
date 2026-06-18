package pl.usos2.server.config;

import pl.usos2.server.service.admin.EmployeeService;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.auth.AuthService;
import pl.usos2.server.service.course.CourseService;
import pl.usos2.server.service.finance.PaymentService;
import pl.usos2.server.service.grade.GradeService;
import pl.usos2.server.service.maintenance.ServiceTicketService;
import pl.usos2.server.service.message.MessageService;
import pl.usos2.server.service.rental.RentalService;
import pl.usos2.server.service.request.RequestService;
import pl.usos2.server.dao.schedule.ScheduleDao;
import pl.usos2.server.dao.schedule.JdbcScheduleDao;
import pl.usos2.server.service.schedule.ScheduleService;




public class ApplicationContext {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final GradeService gradeService;
    private final MessageService messageService;
    private final RequestService requestService;
    private final PaymentService paymentService;
    private final EmployeeService employeeService;
    private final ServiceTicketService serviceTicketService;
    private final RentalService rentalService;
    private final CourseService courseService;
    private final ScheduleService scheduleService;



    public ApplicationContext() {
        this.auditLogService = AuditLogService.getInstance();
        this.authService = new AuthService();
        this.gradeService = new GradeService();
        this.messageService = new MessageService();
        this.requestService = new RequestService();
        this.paymentService = new PaymentService();
        this.employeeService = new EmployeeService();
        this.serviceTicketService = new ServiceTicketService();
        this.rentalService = new RentalService();
        this.courseService = new CourseService();
        ScheduleDao scheduleDao = new JdbcScheduleDao();
        this.scheduleService = new ScheduleService(scheduleDao);
        
    }

    public ApplicationContext(
            AuthService authService,
            AuditLogService auditLogService,
            GradeService gradeService,
            MessageService messageService,
            RequestService requestService,
            PaymentService paymentService,
            EmployeeService employeeService,
            ServiceTicketService serviceTicketService,
            RentalService rentalService,
            CourseService courseService,
            ScheduleService scheduleService
    ) {
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.gradeService = gradeService;
        this.messageService = messageService;
        this.requestService = requestService;
        this.paymentService = paymentService;
        this.employeeService = employeeService;
        this.serviceTicketService = serviceTicketService;
        this.rentalService = rentalService;
        this.courseService = courseService;
        this.scheduleService = scheduleService;
    }

    public ScheduleService getScheduleService() {
        return scheduleService;
    }


    public AuthService getAuthService() {
        return authService;
    }

    public AuditLogService getAuditLogService() {
        return auditLogService;
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

    public RentalService getRentalService() {
        return rentalService;
    }

    public CourseService getCourseService() {
        return courseService;
    }
}