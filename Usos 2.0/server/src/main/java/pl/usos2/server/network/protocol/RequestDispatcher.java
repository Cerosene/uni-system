package pl.usos2.server.network.protocol;

import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.network.request.ClientRequest;
import pl.usos2.server.network.response.ServerResponse;
import pl.usos2.server.network.session.AuthSession;
import pl.usos2.server.network.session.SessionManager;
import pl.usos2.server.security.AuthorizationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;

public class RequestDispatcher {
    private static final Logger logger = Logger.getLogger(RequestDispatcher.class.getName());

    private final ApplicationContext context;
    private final SessionManager sessionManager;

    public RequestDispatcher(ApplicationContext context, SessionManager sessionManager) {
        this.context = Objects.requireNonNull(context, "ApplicationContext cannot be null.");
        this.sessionManager = Objects.requireNonNull(sessionManager, "SessionManager cannot be null.");
    }

    public ServerResponse dispatch(ClientRequest request) {
        try {
            if (request == null || request.getAction() == null || request.getAction().isBlank()) {
                throw new IllegalArgumentException("Request action cannot be empty.");
            }

            logger.info("[API] action=" + request.getAction() + ", token=" + maskToken(request.getSessionToken()));
            Object result = route(request);
            return ServerResponse.ok(result);
        } catch (Throwable throwable) {
            logger.warning("[API] request failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            return ServerResponse.error(throwable);
        }
    }

    private Object route(ClientRequest request) {
        return switch (request.getAction()) {
            case ApiAction.AUTH_LOGIN -> login(request);
            case ApiAction.AUTH_LOGOUT -> logout(request);
            case ApiAction.AUTH_REGISTER -> register(request);
            case ApiAction.USER_FIND_BY_ID -> findUserById(request);
            case ApiAction.USER_FIND_BY_EMAIL -> findUserByEmail(request);
            case ApiAction.USER_LIST_ALL -> listAllUsers(request);
            case ApiAction.USER_LIST_BY_ROLE -> listUsersByRole(request);
            case ApiAction.USER_ACTIVATE -> activateUser(request);
            case ApiAction.USER_DEACTIVATE -> deactivateUser(request);
            case ApiAction.USER_DELETE -> deleteUser(request);
            case ApiAction.USER_CHANGE_ROLE -> changeUserRole(request);
            case ApiAction.USER_UPDATE_BASIC -> updateUserBasic(request);
            case ApiAction.USER_CHANGE_EMAIL -> changeEmail(request);
            case ApiAction.USER_CHANGE_PASSWORD -> changePassword(request);

            case ApiAction.GRADE_ADD -> addGrade(request);
            case ApiAction.GRADE_UPDATE -> updateGrade(request);
            case ApiAction.GRADE_FIND_BY_ID -> context.getGradeService().findById(longArg(request, "gradeId"));
            case ApiAction.GRADE_LIST_STUDENT -> listGradesForStudent(request);
            case ApiAction.GRADE_LIST_COURSE -> listGradesForCourse(request);
            case ApiAction.GRADE_LIST_LECTURER -> listGradesForLecturer(request);
            case ApiAction.GRADE_LIST_ALL -> listAllGrades(request);

            case ApiAction.MESSAGE_SEND -> sendMessage(request);
            case ApiAction.MESSAGE_MARK_READ -> markMessageRead(request);
            case ApiAction.MESSAGE_FIND_BY_ID -> context.getMessageService().findById(longArg(request, "messageId"));
            case ApiAction.MESSAGE_INBOX -> messageInbox(request);
            case ApiAction.MESSAGE_UNREAD_INBOX -> unreadMessageInbox(request);
            case ApiAction.MESSAGE_SENT -> sentMessages(request);
            case ApiAction.MESSAGE_LIST_ALL -> listAllMessages(request);

            case ApiAction.REQUEST_SUBMIT -> submitRequest(request);
            case ApiAction.REQUEST_CHANGE_STATUS -> changeRequestStatus(request);
            case ApiAction.REQUEST_FIND_BY_ID -> context.getRequestService().findById(longArg(request, "requestId"));
            case ApiAction.REQUEST_LIST_STUDENT -> listRequestsByStudent(request);
            case ApiAction.REQUEST_LIST_STATUS -> listRequestsByStatus(request);
            case ApiAction.REQUEST_LIST_TYPE -> listRequestsByType(request);
            case ApiAction.REQUEST_LIST_PENDING -> pendingRequests(request);
            case ApiAction.REQUEST_LIST_ALL -> listAllRequests(request);

            case ApiAction.PAYMENT_CREATE -> createPayment(request);
            case ApiAction.PAYMENT_MARK_PAID -> markPaymentPaid(request);
            case ApiAction.PAYMENT_MARK_UNPAID -> markPaymentUnpaid(request);
            case ApiAction.PAYMENT_REMOVE -> removePayment(request);
            case ApiAction.PAYMENT_FIND_BY_ID -> context.getPaymentService().findById(longArg(request, "paymentId"));
            case ApiAction.PAYMENT_LIST_STUDENT -> listPaymentsForStudent(request);
            case ApiAction.PAYMENT_LIST_UNPAID_STUDENT -> listUnpaidPaymentsForStudent(request);
            case ApiAction.PAYMENT_LIST_PAID_STUDENT -> listPaidPaymentsForStudent(request);
            case ApiAction.PAYMENT_LIST_OVERDUE -> overduePayments(request);
            case ApiAction.PAYMENT_LIST_OVERDUE_STUDENT -> overduePaymentsForStudent(request);
            case ApiAction.PAYMENT_LIST_ALL -> listAllPayments(request);

            case ApiAction.EMPLOYEE_ADD -> addEmployee(request);
            case ApiAction.EMPLOYEE_UPDATE -> updateEmployee(request);
            case ApiAction.EMPLOYEE_CHANGE_POSITION -> changeEmployeePosition(request);
            case ApiAction.EMPLOYEE_CHANGE_SALARY -> changeEmployeeSalary(request);
            case ApiAction.EMPLOYEE_CHANGE_NUMBER -> changeEmployeeNumber(request);
            case ApiAction.EMPLOYEE_SET_ACTIVE -> setEmployeeActive(request);
            case ApiAction.EMPLOYEE_FIND_BY_ID -> context.getEmployeeService().findById(longArg(request, "employeeId"));
            case ApiAction.EMPLOYEE_FIND_BY_EMAIL -> context.getEmployeeService().findByEmail(stringArg(request, "email"));
            case ApiAction.EMPLOYEE_FIND_BY_NUMBER -> context.getEmployeeService().findByEmployeeNumber(stringArg(request, "employeeNumber"));
            case ApiAction.EMPLOYEE_REMOVE -> removeEmployee(request);
            case ApiAction.EMPLOYEE_LIST_ALL -> listAllEmployees(request);
            case ApiAction.EMPLOYEE_LIST_ACTIVE -> listActiveEmployees(request);

            case ApiAction.TICKET_CREATE -> createTicket(request);
            case ApiAction.TICKET_ASSIGN -> assignTicket(request);
            case ApiAction.TICKET_REASSIGN -> reassignTicket(request);
            case ApiAction.TICKET_CLOSE -> closeTicket(request);
            case ApiAction.TICKET_CHANGE_STATUS -> changeTicketStatus(request);
            case ApiAction.TICKET_FIND_BY_ID -> context.getServiceTicketService().findById(longArg(request, "ticketId"));
            case ApiAction.TICKET_FIND_BY_TITLE -> context.getServiceTicketService().findByTitle(stringArg(request, "title"));
            case ApiAction.TICKET_LIST_STATUS -> ticketsByStatus(request);
            case ApiAction.TICKET_LIST_REPORTER -> ticketsByReporter(request);
            case ApiAction.TICKET_LIST_ASSIGNED -> ticketsAssignedTo(request);
            case ApiAction.TICKET_LIST_ALL -> listAllTickets(request);

            case ApiAction.RENTAL_CREATE -> createRental(request);
            case ApiAction.RENTAL_RETURN -> returnRental(request);
            case ApiAction.RENTAL_EXTEND -> extendRental(request);
            case ApiAction.RENTAL_FIND_BY_ID -> context.getRentalService().findById(longArg(request, "rentalId"));
            case ApiAction.RENTAL_LIST_BORROWER -> rentalsForBorrower(request);
            case ApiAction.RENTAL_LIST_RESOURCE -> context.getRentalService().getRentalsForResource(stringArg(request, "resourceName"));
            case ApiAction.RENTAL_LIST_ACTIVE -> activeRentals(request);
            case ApiAction.RENTAL_LIST_RETURNED -> returnedRentals(request);
            case ApiAction.RENTAL_LIST_OVERDUE -> overdueRentals(request);
            case ApiAction.RENTAL_LIST_ALL -> listAllRentals(request);
            case ApiAction.RENTAL_LIST_AVAILABLE_RESOURCES -> context.getRentalService().getAvailableResources();

            case ApiAction.COURSE_ADD -> addCourse(request);
            case ApiAction.COURSE_LIST_ALL -> context.getCourseService().getAllCourses();
            case ApiAction.COURSE_LIST_LECTURER -> coursesForLecturer(request);
            case ApiAction.COURSE_GROUP_ADD -> addGroup(request);
            case ApiAction.COURSE_GROUP_LIST_ALL -> context.getCourseService().getAllGroups();
            case ApiAction.COURSE_GROUP_LIST_STUDENT -> groupsForStudent(request);
            case ApiAction.COURSE_GROUP_LIST_LECTURER -> groupsForLecturer(request);
            case ApiAction.COURSE_GROUP_STUDENTS -> studentsForGroup(request);
            case ApiAction.COURSE_LECTURER_ACTIVE_STUDENT_COUNT -> countActiveStudentsForLecturer(request);
            case ApiAction.COURSE_ENROLL_STUDENT -> enrollStudent(request);
            case ApiAction.COURSE_REMOVE_STUDENT -> removeStudentFromGroup(request);
            case ApiAction.COURSE_SCHEDULE_GROUPS -> context.getCourseService().getScheduleGroups();

            default -> throw new IllegalArgumentException("Unsupported API action: " + request.getAction());
        };
    }

    private AuthSession login(ClientRequest request) {
        User user = context.getAuthService().login(stringArg(request, "email"), stringArg(request, "password"));
        return sessionManager.createSession(user);
    }

    private Object logout(ClientRequest request) {
        User user = requireUser(request);
        context.getAuthService().logout(user.getId());
        sessionManager.invalidate(request.getSessionToken());
        return null;
    }

    private Object register(ClientRequest request) {
        User caller = requireUser(request);
        AuthorizationService.requireRole(caller, UserRole.ADMINISTRATOR);
        return context.getAuthService().register(arg(request, "user", User.class));
    }

    private Object findUserById(ClientRequest request) {
        User caller = requireUser(request);
        Long userId = longArg(request, "userId");
        AuthorizationService.requireSelfOrAnyRole(caller, userId, UserRole.ADMINISTRATOR);
        return context.getAuthService().findById(userId);
    }

    private Object findUserByEmail(ClientRequest request) {
        requireUser(request);
        return context.getAuthService().findByEmail(stringArg(request, "email"));
    }

    private Object listAllUsers(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getAuthService().getAllUsers();
    }

    private Object listUsersByRole(ClientRequest request) {
        requireUser(request);
        return context.getAuthService().getUsersByRole(arg(request, "role", UserRole.class));
    }

    private Object activateUser(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getAuthService().activateUser(longArg(request, "userId"));
    }

    private Object deactivateUser(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getAuthService().deactivateUser(longArg(request, "userId"));
    }

    private Object deleteUser(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getAuthService().deleteUser(longArg(request, "userId"));
        return null;
    }

    private Object changeUserRole(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getAuthService().changeRole(longArg(request, "userId"), arg(request, "role", UserRole.class));
    }

    private Object updateUserBasic(ClientRequest request) {
        User caller = requireUser(request);
        Long userId = longArg(request, "userId");
        AuthorizationService.requireSelfOrAnyRole(caller, userId, UserRole.ADMINISTRATOR);
        return context.getAuthService().updateBasicData(userId, stringArg(request, "firstName"), stringArg(request, "lastName"));
    }

    private Object changeEmail(ClientRequest request) {
        User caller = requireUser(request);
        Long userId = longArg(request, "userId");
        AuthorizationService.requireSelfOrAnyRole(caller, userId, UserRole.ADMINISTRATOR);
        return context.getAuthService().changeEmail(userId, stringArg(request, "newEmail"));
    }

    private Object changePassword(ClientRequest request) {
        User caller = requireUser(request);
        Long userId = longArg(request, "userId");
        AuthorizationService.requireSelfOrAnyRole(caller, userId, UserRole.ADMINISTRATOR);
        return context.getAuthService().changePassword(userId, stringArg(request, "currentPassword"), stringArg(request, "newPassword"));
    }

    private Object addGrade(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.LECTURER);
        return context.getGradeService().addGrade(
                arg(request, "student", Student.class),
                arg(request, "course", Course.class),
                arg(request, "lecturer", Lecturer.class),
                doubleArg(request, "value"),
                stringArg(request, "description")
        );
    }

    private Object updateGrade(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.LECTURER);
        return context.getGradeService().updateGrade(arg(request, "grade", Grade.class), doubleArg(request, "value"), stringArg(request, "description"));
    }

    private Object listGradesForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR, UserRole.LECTURER);
        return context.getGradeService().getGradesForStudent(student);
    }

    private Object listGradesForCourse(ClientRequest request) {
        AuthorizationService.requireAnyRole(requireUser(request), UserRole.LECTURER, UserRole.ADMINISTRATOR);
        return context.getGradeService().getGradesForCourse(arg(request, "course", Course.class));
    }

    private Object listGradesForLecturer(ClientRequest request) {
        User caller = requireUser(request);
        Lecturer lecturer = arg(request, "lecturer", Lecturer.class);
        AuthorizationService.requireSelfOrAnyRole(caller, lecturer.getId(), UserRole.ADMINISTRATOR);
        return context.getGradeService().getGradesForLecturer(lecturer);
    }

    private Object listAllGrades(ClientRequest request) {
        AuthorizationService.requireAnyRole(requireUser(request), UserRole.LECTURER, UserRole.ADMINISTRATOR);
        return context.getGradeService().getAllGrades();
    }

    private Object sendMessage(ClientRequest request) {
        User caller = requireUser(request);
        User sender = arg(request, "sender", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, sender.getId(), UserRole.ADMINISTRATOR);
        return context.getMessageService().sendMessage(sender, arg(request, "recipient", User.class), stringArg(request, "subject"), stringArg(request, "content"));
    }

    private Object markMessageRead(ClientRequest request) {
        User caller = requireUser(request);
        User reader = arg(request, "reader", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, reader.getId(), UserRole.ADMINISTRATOR);
        return context.getMessageService().markAsRead(arg(request, "message", Message.class), reader);
    }

    private Object messageInbox(ClientRequest request) {
        User caller = requireUser(request);
        User recipient = arg(request, "recipient", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, recipient.getId(), UserRole.ADMINISTRATOR);
        return context.getMessageService().getInbox(recipient);
    }

    private Object unreadMessageInbox(ClientRequest request) {
        User caller = requireUser(request);
        User recipient = arg(request, "recipient", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, recipient.getId(), UserRole.ADMINISTRATOR);
        return context.getMessageService().getUnreadInbox(recipient);
    }

    private Object sentMessages(ClientRequest request) {
        User caller = requireUser(request);
        User sender = arg(request, "sender", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, sender.getId(), UserRole.ADMINISTRATOR);
        return context.getMessageService().getSentMessages(sender);
    }

    private Object listAllMessages(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getMessageService().getAllMessages();
    }

    private Object submitRequest(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getRequestService().submitRequest(student, arg(request, "type", RequestType.class), stringArg(request, "content"));
    }

    private Object changeRequestStatus(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getRequestService().changeStatus(arg(request, "request", Request.class), arg(request, "status", RequestStatus.class));
        return null;
    }

    private Object listRequestsByStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getRequestService().getRequestsByStudent(student);
    }

    private Object listRequestsByStatus(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRequestService().getRequestsByStatus(arg(request, "status", RequestStatus.class));
    }

    private Object listRequestsByType(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRequestService().getRequestsByType(arg(request, "type", RequestType.class));
    }

    private Object pendingRequests(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRequestService().getPendingRequests();
    }

    private Object listAllRequests(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRequestService().getAllRequests();
    }

    private Object createPayment(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getPaymentService().createPayment(
                longArg(request, "paymentId"),
                arg(request, "student", Student.class),
                arg(request, "amount", BigDecimal.class),
                stringArg(request, "title"),
                arg(request, "dueDate", LocalDate.class)
        );
    }

    private Object markPaymentPaid(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getPaymentService().markAsPaid(longArg(request, "paymentId"));
    }

    private Object markPaymentUnpaid(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getPaymentService().markAsUnpaid(longArg(request, "paymentId"));
    }

    private Object removePayment(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getPaymentService().removePayment(longArg(request, "paymentId"));
        return null;
    }

    private Object listPaymentsForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getPaymentsForStudent(student);
    }

    private Object listUnpaidPaymentsForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getUnpaidPaymentsForStudent(student);
    }

    private Object listPaidPaymentsForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getPaidPaymentsForStudent(student);
    }

    private Object overduePayments(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getOverduePayments(arg(request, "currentDate", LocalDate.class));
    }

    private Object overduePaymentsForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getOverduePaymentsForStudent(student, arg(request, "currentDate", LocalDate.class));
    }

    private Object listAllPayments(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getPaymentService().getAllPayments();
    }

    private Object addEmployee(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().addEmployee(arg(request, "employee", Employee.class));
    }

    private Object updateEmployee(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().updateEmployee(longArg(request, "employeeId"), stringArg(request, "firstName"), stringArg(request, "lastName"), stringArg(request, "email"), stringArg(request, "position"));
    }

    private Object changeEmployeePosition(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().changePosition(longArg(request, "employeeId"), stringArg(request, "position"));
    }

    private Object changeEmployeeSalary(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().changeSalary(longArg(request, "employeeId"), arg(request, "salary", BigDecimal.class));
    }

    private Object changeEmployeeNumber(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().changeEmployeeNumber(longArg(request, "employeeId"), stringArg(request, "employeeNumber"));
    }

    private Object setEmployeeActive(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().setEmployeeActive(longArg(request, "employeeId"), boolArg(request, "active"));
    }

    private Object removeEmployee(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getEmployeeService().removeEmployee(longArg(request, "employeeId"));
        return null;
    }

    private Object listAllEmployees(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().getAllEmployees();
    }

    private Object listActiveEmployees(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getEmployeeService().getActiveEmployees();
    }

    private Object createTicket(ClientRequest request) {
        User caller = requireUser(request);
        User reporter = arg(request, "reporter", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, reporter.getId(), UserRole.ADMINISTRATOR);
        return context.getServiceTicketService().createTicket(reporter, stringArg(request, "title"), stringArg(request, "description"));
    }

    private Object assignTicket(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getServiceTicketService().assignTicket(arg(request, "ticket", ServiceTicket.class), arg(request, "administrator", Administrator.class));
        return null;
    }

    private Object reassignTicket(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getServiceTicketService().reassignTicket(arg(request, "ticket", ServiceTicket.class), arg(request, "administrator", Administrator.class));
        return null;
    }

    private Object closeTicket(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getServiceTicketService().closeTicket(arg(request, "ticket", ServiceTicket.class));
        return null;
    }

    private Object changeTicketStatus(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getServiceTicketService().changeStatus(arg(request, "ticket", ServiceTicket.class), arg(request, "status", ServiceTicketStatus.class));
        return null;
    }

    private Object ticketsByStatus(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getServiceTicketService().getTicketsByStatus(arg(request, "status", ServiceTicketStatus.class));
    }

    private Object ticketsByReporter(ClientRequest request) {
        User caller = requireUser(request);
        User reporter = arg(request, "reporter", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, reporter.getId(), UserRole.ADMINISTRATOR);
        return context.getServiceTicketService().getTicketsByReporter(reporter);
    }

    private Object ticketsAssignedTo(ClientRequest request) {
        User caller = requireUser(request);
        Administrator administrator = arg(request, "administrator", Administrator.class);
        AuthorizationService.requireSelfOrAnyRole(caller, administrator.getId(), UserRole.ADMINISTRATOR);
        return context.getServiceTicketService().getTicketsAssignedTo(administrator);
    }

    private Object listAllTickets(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getServiceTicketService().getAllTickets();
    }

    private Object createRental(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().createRental(longArg(request, "rentalId"), arg(request, "borrower", User.class), stringArg(request, "resourceName"), arg(request, "rentalDate", LocalDate.class), arg(request, "returnDate", LocalDate.class));
    }

    private Object returnRental(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().returnRental(longArg(request, "rentalId"));
    }

    private Object extendRental(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().extendReturnDate(longArg(request, "rentalId"), arg(request, "returnDate", LocalDate.class));
    }

    private Object rentalsForBorrower(ClientRequest request) {
        User caller = requireUser(request);
        User borrower = arg(request, "borrower", User.class);
        AuthorizationService.requireSelfOrAnyRole(caller, borrower.getId(), UserRole.ADMINISTRATOR);
        return context.getRentalService().getRentalsForBorrower(borrower);
    }

    private Object activeRentals(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().getActiveRentals();
    }

    private Object returnedRentals(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().getReturnedRentals();
    }

    private Object overdueRentals(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().getOverdueRentals(arg(request, "currentDate", LocalDate.class));
    }

    private Object listAllRentals(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getRentalService().getAllRentals();
    }

    private Object addCourse(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getCourseService().addCourse(arg(request, "course", Course.class));
    }

    private Object coursesForLecturer(ClientRequest request) {
        User caller = requireUser(request);
        Lecturer lecturer = arg(request, "lecturer", Lecturer.class);
        AuthorizationService.requireSelfOrAnyRole(caller, lecturer.getId(), UserRole.ADMINISTRATOR);
        return context.getCourseService().getCoursesForLecturer(lecturer);
    }

    private Object addGroup(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        return context.getCourseService().addGroup(arg(request, "group", StudentGroup.class));
    }

    private Object groupsForStudent(ClientRequest request) {
        User caller = requireUser(request);
        Student student = arg(request, "student", Student.class);
        AuthorizationService.requireSelfOrAnyRole(caller, student.getId(), UserRole.ADMINISTRATOR);
        return context.getCourseService().getGroupsForStudent(student);
    }

    private Object groupsForLecturer(ClientRequest request) {
        User caller = requireUser(request);
        Lecturer lecturer = arg(request, "lecturer", Lecturer.class);
        AuthorizationService.requireSelfOrAnyRole(caller, lecturer.getId(), UserRole.ADMINISTRATOR);
        return context.getCourseService().getGroupsForLecturer(lecturer);
    }

    private Object studentsForGroup(ClientRequest request) {
        AuthorizationService.requireAnyRole(requireUser(request), UserRole.LECTURER, UserRole.ADMINISTRATOR);
        return context.getCourseService().getStudentsForGroup(longArg(request, "groupId"));
    }

    private Object countActiveStudentsForLecturer(ClientRequest request) {
        User caller = requireUser(request);
        Lecturer lecturer = arg(request, "lecturer", Lecturer.class);
        AuthorizationService.requireSelfOrAnyRole(caller, lecturer.getId(), UserRole.ADMINISTRATOR);
        return context.getCourseService().countActiveStudentsForLecturer(lecturer);
    }

    private Object enrollStudent(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getCourseService().enrollStudentToGroup(arg(request, "student", Student.class), arg(request, "group", StudentGroup.class));
        return null;
    }

    private Object removeStudentFromGroup(ClientRequest request) {
        AuthorizationService.requireRole(requireUser(request), UserRole.ADMINISTRATOR);
        context.getCourseService().removeStudentFromGroup(arg(request, "student", Student.class), arg(request, "group", StudentGroup.class));
        return null;
    }

    private User requireUser(ClientRequest request) {
        return sessionManager.requireUser(request.getSessionToken());
    }

    private String stringArg(ClientRequest request, String key) {
        Object value = request.get(key);
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Missing string payload: " + key);
        }
        return text;
    }

    private Long longArg(ClientRequest request, String key) {
        Object value = request.get(key);
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Integer number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        throw new IllegalArgumentException("Missing long payload: " + key);
    }

    private double doubleArg(ClientRequest request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text.trim());
        }
        throw new IllegalArgumentException("Missing double payload: " + key);
    }

    private boolean boolArg(ClientRequest request, String key) {
        Object value = request.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text.trim());
        }
        throw new IllegalArgumentException("Missing boolean payload: " + key);
    }

    private <T> T arg(ClientRequest request, String key, Class<T> type) {
        Object value = request.get(key);
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Missing payload '" + key + "' of type " + type.getSimpleName() + ".");
        }
        return type.cast(value);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "-";
        }
        return token.substring(0, 8) + "...";
    }
}
