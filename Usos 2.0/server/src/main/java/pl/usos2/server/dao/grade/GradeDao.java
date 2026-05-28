package pl.usos2.server.dao.grade;

import pl.usos2.server.model.academic.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeDao {
    Grade save(Long studentId, Long subjectId, Long lecturerId, double value, String description);

    Grade update(Long gradeId, double value, String description);

    Optional<Grade> findById(Long gradeId);

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findBySubjectId(Long subjectId);

    List<Grade> findByLecturerId(Long lecturerId);

    List<Grade> findAll();

    boolean existsDuplicate(Long studentId, Long subjectId, String description);
}

