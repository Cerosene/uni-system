package pl.usos2.server.service.grade;

import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import java.util.ArrayList;
import java.util.List;

public class GradeService {
    private final List<Grade> grades = new ArrayList<>();

    public Grade addGrade(Student student, Course course, Lecturer lecturer, double value, String description) {
        if (student == null || course == null || lecturer == null) {
            throw new IllegalArgumentException("Student, course and lecturer cannot be null.");
        }
        if (value < 2.0 || value > 5.0) {
            throw new IllegalArgumentException("Grade value must be between 2.0 and 5.0.");
        }

        Grade grade = new Grade(null, student, course, lecturer, value, description);
        grades.add(grade);
        return grade;
    }

    public List<Grade> getAllGrades() {
        return new ArrayList<>(grades);
    }
}