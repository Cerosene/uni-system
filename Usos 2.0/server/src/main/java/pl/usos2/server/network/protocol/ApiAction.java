package pl.usos2.server.network.protocol;

public final class ApiAction {
    private ApiAction() {
    }

    public static final String AUTH_LOGIN = "AUTH_LOGIN";
    public static final String AUTH_LOGOUT = "AUTH_LOGOUT";
    public static final String AUTH_REGISTER = "AUTH_REGISTER";
    public static final String USER_FIND_BY_ID = "USER_FIND_BY_ID";
    public static final String USER_FIND_BY_EMAIL = "USER_FIND_BY_EMAIL";
    public static final String USER_LIST_ALL = "USER_LIST_ALL";
    public static final String USER_LIST_BY_ROLE = "USER_LIST_BY_ROLE";
    public static final String USER_ACTIVATE = "USER_ACTIVATE";
    public static final String USER_DEACTIVATE = "USER_DEACTIVATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_CHANGE_ROLE = "USER_CHANGE_ROLE";
    public static final String USER_UPDATE_BASIC = "USER_UPDATE_BASIC";
    public static final String USER_CHANGE_EMAIL = "USER_CHANGE_EMAIL";
    public static final String USER_CHANGE_PASSWORD = "USER_CHANGE_PASSWORD";

    public static final String GRADE_ADD = "GRADE_ADD";
    public static final String GRADE_UPDATE = "GRADE_UPDATE";
    public static final String GRADE_FIND_BY_ID = "GRADE_FIND_BY_ID";
    public static final String GRADE_LIST_STUDENT = "GRADE_LIST_STUDENT";
    public static final String GRADE_LIST_COURSE = "GRADE_LIST_COURSE";
    public static final String GRADE_LIST_LECTURER = "GRADE_LIST_LECTURER";
    public static final String GRADE_LIST_ALL = "GRADE_LIST_ALL";

    public static final String MESSAGE_SEND = "MESSAGE_SEND";
    public static final String MESSAGE_MARK_READ = "MESSAGE_MARK_READ";
    public static final String MESSAGE_FIND_BY_ID = "MESSAGE_FIND_BY_ID";
    public static final String MESSAGE_INBOX = "MESSAGE_INBOX";
    public static final String MESSAGE_UNREAD_INBOX = "MESSAGE_UNREAD_INBOX";
    public static final String MESSAGE_SENT = "MESSAGE_SENT";
    public static final String MESSAGE_LIST_ALL = "MESSAGE_LIST_ALL";

    public static final String REQUEST_SUBMIT = "REQUEST_SUBMIT";
    public static final String REQUEST_CHANGE_STATUS = "REQUEST_CHANGE_STATUS";
    public static final String REQUEST_FIND_BY_ID = "REQUEST_FIND_BY_ID";
    public static final String REQUEST_LIST_STUDENT = "REQUEST_LIST_STUDENT";
    public static final String REQUEST_LIST_STATUS = "REQUEST_LIST_STATUS";
    public static final String REQUEST_LIST_TYPE = "REQUEST_LIST_TYPE";
    public static final String REQUEST_LIST_PENDING = "REQUEST_LIST_PENDING";
    public static final String REQUEST_LIST_ALL = "REQUEST_LIST_ALL";

    public static final String PAYMENT_CREATE = "PAYMENT_CREATE";
    public static final String PAYMENT_MARK_PAID = "PAYMENT_MARK_PAID";
    public static final String PAYMENT_MARK_UNPAID = "PAYMENT_MARK_UNPAID";
    public static final String PAYMENT_REMOVE = "PAYMENT_REMOVE";
    public static final String PAYMENT_FIND_BY_ID = "PAYMENT_FIND_BY_ID";
    public static final String PAYMENT_LIST_STUDENT = "PAYMENT_LIST_STUDENT";
    public static final String PAYMENT_LIST_UNPAID_STUDENT = "PAYMENT_LIST_UNPAID_STUDENT";
    public static final String PAYMENT_LIST_PAID_STUDENT = "PAYMENT_LIST_PAID_STUDENT";
    public static final String PAYMENT_LIST_OVERDUE = "PAYMENT_LIST_OVERDUE";
    public static final String PAYMENT_LIST_OVERDUE_STUDENT = "PAYMENT_LIST_OVERDUE_STUDENT";
    public static final String PAYMENT_LIST_ALL = "PAYMENT_LIST_ALL";

    public static final String EMPLOYEE_ADD = "EMPLOYEE_ADD";
    public static final String EMPLOYEE_UPDATE = "EMPLOYEE_UPDATE";
    public static final String EMPLOYEE_CHANGE_POSITION = "EMPLOYEE_CHANGE_POSITION";
    public static final String EMPLOYEE_CHANGE_SALARY = "EMPLOYEE_CHANGE_SALARY";
    public static final String EMPLOYEE_CHANGE_NUMBER = "EMPLOYEE_CHANGE_NUMBER";
    public static final String EMPLOYEE_SET_ACTIVE = "EMPLOYEE_SET_ACTIVE";
    public static final String EMPLOYEE_FIND_BY_ID = "EMPLOYEE_FIND_BY_ID";
    public static final String EMPLOYEE_FIND_BY_EMAIL = "EMPLOYEE_FIND_BY_EMAIL";
    public static final String EMPLOYEE_FIND_BY_NUMBER = "EMPLOYEE_FIND_BY_NUMBER";
    public static final String EMPLOYEE_REMOVE = "EMPLOYEE_REMOVE";
    public static final String EMPLOYEE_LIST_ALL = "EMPLOYEE_LIST_ALL";
    public static final String EMPLOYEE_LIST_ACTIVE = "EMPLOYEE_LIST_ACTIVE";

    public static final String TICKET_CREATE = "TICKET_CREATE";
    public static final String TICKET_ASSIGN = "TICKET_ASSIGN";
    public static final String TICKET_REASSIGN = "TICKET_REASSIGN";
    public static final String TICKET_CLOSE = "TICKET_CLOSE";
    public static final String TICKET_CHANGE_STATUS = "TICKET_CHANGE_STATUS";
    public static final String TICKET_FIND_BY_ID = "TICKET_FIND_BY_ID";
    public static final String TICKET_FIND_BY_TITLE = "TICKET_FIND_BY_TITLE";
    public static final String TICKET_LIST_STATUS = "TICKET_LIST_STATUS";
    public static final String TICKET_LIST_REPORTER = "TICKET_LIST_REPORTER";
    public static final String TICKET_LIST_ASSIGNED = "TICKET_LIST_ASSIGNED";
    public static final String TICKET_LIST_ALL = "TICKET_LIST_ALL";

    public static final String RENTAL_CREATE = "RENTAL_CREATE";
    public static final String RENTAL_RETURN = "RENTAL_RETURN";
    public static final String RENTAL_EXTEND = "RENTAL_EXTEND";
    public static final String RENTAL_FIND_BY_ID = "RENTAL_FIND_BY_ID";
    public static final String RENTAL_LIST_BORROWER = "RENTAL_LIST_BORROWER";
    public static final String RENTAL_LIST_RESOURCE = "RENTAL_LIST_RESOURCE";
    public static final String RENTAL_LIST_ACTIVE = "RENTAL_LIST_ACTIVE";
    public static final String RENTAL_LIST_RETURNED = "RENTAL_LIST_RETURNED";
    public static final String RENTAL_LIST_OVERDUE = "RENTAL_LIST_OVERDUE";
    public static final String RENTAL_LIST_ALL = "RENTAL_LIST_ALL";
    public static final String RENTAL_LIST_AVAILABLE_RESOURCES = "RENTAL_LIST_AVAILABLE_RESOURCES";

    public static final String COURSE_ADD = "COURSE_ADD";
    public static final String COURSE_LIST_ALL = "COURSE_LIST_ALL";
    public static final String COURSE_LIST_LECTURER = "COURSE_LIST_LECTURER";
    public static final String COURSE_GROUP_ADD = "COURSE_GROUP_ADD";
    public static final String COURSE_GROUP_LIST_ALL = "COURSE_GROUP_LIST_ALL";
    public static final String COURSE_GROUP_LIST_STUDENT = "COURSE_GROUP_LIST_STUDENT";
    public static final String COURSE_GROUP_LIST_LECTURER = "COURSE_GROUP_LIST_LECTURER";
    public static final String COURSE_GROUP_STUDENTS = "COURSE_GROUP_STUDENTS";
    public static final String COURSE_LECTURER_ACTIVE_STUDENT_COUNT = "COURSE_LECTURER_ACTIVE_STUDENT_COUNT";
    public static final String COURSE_ENROLL_STUDENT = "COURSE_ENROLL_STUDENT";
    public static final String COURSE_REMOVE_STUDENT = "COURSE_REMOVE_STUDENT";
    public static final String COURSE_SCHEDULE_GROUPS = "COURSE_SCHEDULE_GROUPS";
}
