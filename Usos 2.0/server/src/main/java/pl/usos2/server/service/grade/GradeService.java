package pl.usos2.server.service.grade;

import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GradeService {
    private static final Logger logger = Logger.getLogger(GradeService.class.getName());

    private final List<Grade> grades = new ArrayList<>();
    private long nextGradeId = 1L;

    public Grade addGrade(Student student, Course course, Lecturer lecturer, double value, String description) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(course, "Course cannot be null.");
        ValidationUtils.requireNotNull(course.getId(), "Course id cannot be null.");
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");
        ValidationUtils.requireNotBlank(description, "Grade description cannot be empty.");

        validateGradeValue(value);

        String normalizedDescription = description.trim();

        boolean duplicateExists = grades.stream()
                .anyMatch(grade ->
                        grade.getStudent().getId().equals(student.getId())
                                && grade.getCourse().getId().equals(course.getId())
                                && grade.getDescription().equalsIgnoreCase(normalizedDescription)
                );

        if (duplicateExists) {
            logger.warning("Cannot add grade. Duplicate grade entry for student: " + student.getFullName());
            throw new IllegalArgumentException("Similar grade already exists for this student and course.");
        }

        Grade grade = new Grade(nextGradeId++, student, course, lecturer, value, normalizedDescription);
        grades.add(grade);

        logger.info("Added grade " + value + " for student: " + student.getFullName()
                + ", course: " + course.getCode());
        return grade;
    }

    public Grade updateGrade(Grade grade, double newValue, String newDescription) {
        ValidationUtils.requireNotNull(grade, "Grade cannot be null.");
        ValidationUtils.requireNotBlank(newDescription, "Grade description cannot be empty.");

        validateGradeValue(newValue);

        grade.setValue(newValue);
        grade.setDescription(newDescription.trim());

        logger.info("Updated grade for student: " + grade.getStudent().getFullName()
                + ", new value: " + newValue);
        return grade;
    }

    public Grade findById(Long gradeId) {
        ValidationUtils.requireNotNull(gradeId, "Grade id cannot be null.");

        return grades.stream()
                .filter(grade -> gradeId.equals(grade.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grade not found."));
    }

    public List<Grade> getGradesForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        return grades.stream()
                .filter(grade -> grade.getStudent().getId().equals(student.getId()))
                .toList();
    }

    public List<Grade> getGradesForCourse(Course course) {
        ValidationUtils.requireNotNull(course, "Course cannot be null.");
        ValidationUtils.requireNotNull(course.getId(), "Course id cannot be null.");

        return grades.stream()
                .filter(grade -> grade.getCourse().getId().equals(course.getId()))
                .toList();
    }

    public List<Grade> getGradesForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");

        return grades.stream()
                .filter(grade -> grade.getLecturer().getId().equals(lecturer.getId()))
                .toList();
    }

    public double getAverageGradeForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Grade> studentGrades = getGradesForStudent(student);

        if (studentGrades.isEmpty()) {
            return 0.0;
        }

        return studentGrades.stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);
    }

    public List<Grade> getAllGrades() {
        return new ArrayList<>(grades);
    }

    private void validateGradeValue(double value) {
        if (value < 2.0 || value > 5.0) {
            logger.warning("Invalid grade value: " + value);
            throw new IllegalArgumentException("Grade value must be between 2.0 and 5.0.");
        }
    }
}