package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.course.CourseService;

import java.util.List;

public class RemoteCourseService extends CourseService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteCourseService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Course addCourse(Course course) {
        return (Course) apiClient.send(ApiAction.COURSE_ADD, session.getToken(), apiClient.payload("course", course));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Course> getAllCourses() {
        return (List<Course>) apiClient.send(ApiAction.COURSE_LIST_ALL, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Course> getCoursesForLecturer(Lecturer lecturer) {
        return (List<Course>) apiClient.send(ApiAction.COURSE_LIST_LECTURER, session.getToken(), apiClient.payload("lecturer", lecturer));
    }

    @Override
    public StudentGroup addGroup(StudentGroup group) {
        return (StudentGroup) apiClient.send(ApiAction.COURSE_GROUP_ADD, session.getToken(), apiClient.payload("group", group));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StudentGroup> getAllGroups() {
        return (List<StudentGroup>) apiClient.send(ApiAction.COURSE_GROUP_LIST_ALL, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StudentGroup> getGroupsForStudent(Student student) {
        return (List<StudentGroup>) apiClient.send(ApiAction.COURSE_GROUP_LIST_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StudentGroup> getGroupsForLecturer(Lecturer lecturer) {
        return (List<StudentGroup>) apiClient.send(ApiAction.COURSE_GROUP_LIST_LECTURER, session.getToken(), apiClient.payload("lecturer", lecturer));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Student> getStudentsForGroup(Long groupId) {
        return (List<Student>) apiClient.send(ApiAction.COURSE_GROUP_STUDENTS, session.getToken(), apiClient.payload("groupId", groupId));
    }

    @Override
    public int countActiveStudentsForLecturer(Lecturer lecturer) {
        return (Integer) apiClient.send(ApiAction.COURSE_LECTURER_ACTIVE_STUDENT_COUNT, session.getToken(), apiClient.payload("lecturer", lecturer));
    }

    @Override
    public void enrollStudentToGroup(Student student, StudentGroup group) {
        apiClient.send(ApiAction.COURSE_ENROLL_STUDENT, session.getToken(), apiClient.payload("student", student, "group", group));
    }

    @Override
    public void removeStudentFromGroup(Student student, StudentGroup group) {
        apiClient.send(ApiAction.COURSE_REMOVE_STUDENT, session.getToken(), apiClient.payload("student", student, "group", group));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StudentGroup> getScheduleGroups() {
        return (List<StudentGroup>) apiClient.send(ApiAction.COURSE_SCHEDULE_GROUPS, session.getToken());
    }
}
