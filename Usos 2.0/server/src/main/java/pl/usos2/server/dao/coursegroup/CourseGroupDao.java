package pl.usos2.server.dao.coursegroup;

import pl.usos2.server.model.academic.StudentGroup;

import java.util.List;
import java.util.Optional;

public interface CourseGroupDao {
    StudentGroup save(StudentGroup group);

    Optional<StudentGroup> findById(Long groupId);

    List<StudentGroup> findAll();

    List<StudentGroup> findByLecturerId(Long lecturerId);

    List<StudentGroup> findByStudentId(Long studentId);

    int countActiveStudentsByLecturerId(Long lecturerId);

    boolean existsById(Long groupId);
}
