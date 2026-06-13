package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.grade.GradeService;

import java.util.List;

public class RemoteGradeService extends GradeService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteGradeService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Grade addGrade(Student student, Course course, Lecturer lecturer, double value, String description) {
        return (Grade) apiClient.send(ApiAction.GRADE_ADD, session.getToken(), apiClient.payload(
                "student", student, "course", course, "lecturer", lecturer, "value", value, "description", description
        ));
    }

    @Override
    public Grade updateGrade(Grade grade, double newValue, String newDescription) {
        return (Grade) apiClient.send(ApiAction.GRADE_UPDATE, session.getToken(), apiClient.payload(
                "grade", grade, "value", newValue, "description", newDescription
        ));
    }

    @Override
    public Grade findById(Long gradeId) {
        return (Grade) apiClient.send(ApiAction.GRADE_FIND_BY_ID, session.getToken(), apiClient.payload("gradeId", gradeId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Grade> getGradesForStudent(Student student) {
        return (List<Grade>) apiClient.send(ApiAction.GRADE_LIST_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Grade> getGradesForCourse(Course course) {
        return (List<Grade>) apiClient.send(ApiAction.GRADE_LIST_COURSE, session.getToken(), apiClient.payload("course", course));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Grade> getGradesForLecturer(Lecturer lecturer) {
        return (List<Grade>) apiClient.send(ApiAction.GRADE_LIST_LECTURER, session.getToken(), apiClient.payload("lecturer", lecturer));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Grade> getAllGrades() {
        return (List<Grade>) apiClient.send(ApiAction.GRADE_LIST_ALL, session.getToken());
    }

    @Override
    public double getAverageGradeForStudent(Student student) {
        List<Grade> grades = getGradesForStudent(student);

        if (grades.isEmpty()) {
            return 0.0;
        }

        return grades.stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);
    }
}

