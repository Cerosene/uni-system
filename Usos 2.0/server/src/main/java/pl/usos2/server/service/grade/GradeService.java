package pl.usos2.server.service.grade;

import pl.usos2.server.dao.grade.GradeDao;
import pl.usos2.server.dao.grade.JdbcGradeDao;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class GradeService {
    private static final Logger logger = Logger.getLogger(GradeService.class.getName());
    private final GradeDao gradeDao;

    public GradeService() {
        this(new JdbcGradeDao());
    }

    public GradeService(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

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

        boolean duplicateExists = gradeDao.existsDuplicate(student.getId(), course.getId(), normalizedDescription);

        if (duplicateExists) {
            logger.warning("Cannot add grade. Duplicate grade entry for student: " + student.getFullName());
            throw new IllegalArgumentException("Similar grade already exists for this student and course.");
        }

        Grade grade = gradeDao.save(student.getId(), course.getId(), lecturer.getId(), value, normalizedDescription);

        logger.info("Added grade " + value + " for student: " + student.getFullName() + ", course: " + course.getCode());
        logger.info("[DIAGNOSTIC] Grade add persisted in Oracle. gradeId=" + grade.getId());
        return grade;
    }

    public Grade updateGrade(Grade grade, double newValue, String newDescription) {
        ValidationUtils.requireNotNull(grade, "Grade cannot be null.");
        ValidationUtils.requireNotBlank(newDescription, "Grade description cannot be empty.");

        validateGradeValue(newValue);

        Grade updated = gradeDao.update(grade.getId(), newValue, newDescription.trim());

        // Keep behavior from previous in-memory implementation for existing UI references.
        grade.setValue(newValue);
        grade.setDescription(newDescription.trim());

        logger.info("Updated grade for student: " + grade.getStudent().getFullName()
                + ", new value: " + newValue);
        logger.info("[DIAGNOSTIC] Grade update persisted in Oracle. gradeId=" + updated.getId());
        return grade;
    }

    public Grade findById(Long gradeId) {
        ValidationUtils.requireNotNull(gradeId, "Grade id cannot be null.");

        Grade grade = gradeDao.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found."));
        logger.info("[DIAGNOSTIC] Grade loaded from Oracle. gradeId=" + gradeId);
        return grade;
    }

    public List<Grade> getGradesForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Grade> grades = gradeDao.findByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Student grades fetched from Oracle. studentId=" + student.getId()
                + ", count=" + grades.size());
        return grades;
    }

    public List<Grade> getGradesForCourse(Course course) {
        ValidationUtils.requireNotNull(course, "Course cannot be null.");
        ValidationUtils.requireNotNull(course.getId(), "Course id cannot be null.");

        List<Grade> grades = gradeDao.findBySubjectId(course.getId());
        logger.info("[DIAGNOSTIC] Course grades fetched from Oracle. subjectId=" + course.getId()
                + ", count=" + grades.size());
        return grades;
    }

    public List<Grade> getGradesForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        
        List<Grade> grades = ((JdbcGradeDao) gradeDao).getStudentsWithGradesForLecturer(lecturer.getId());
        
        logger.info("[DIAGNOSTIC] Lecturer students loaded. count=" + grades.size());
        return grades;
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
        List<Grade> grades = gradeDao.findAll();
        logger.info("[DIAGNOSTIC] All grades fetched from Oracle. count=" + grades.size());
        return grades;
    }

    private void validateGradeValue(double value) {
        if (value < 2.0 || value > 5.0) {
            logger.warning("Invalid grade value: " + value);
            throw new IllegalArgumentException("Grade value must be between 2.0 and 5.0.");
        }
    }
}
