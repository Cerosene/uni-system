package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.coursegroup.CourseGroupDao;
import pl.usos2.server.model.academic.StudentGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeCourseGroupDao implements CourseGroupDao {
    private final Map<Long, StudentGroup> groups = new ConcurrentHashMap<>();

    @Override
    public StudentGroup save(StudentGroup group) {
        groups.put(group.getId(), group);
        return group;
    }

    @Override
    public Optional<StudentGroup> findById(Long groupId) {
        return Optional.ofNullable(groups.get(groupId));
    }

    @Override
    public List<StudentGroup> findAll() {
        return new ArrayList<>(groups.values());
    }

    @Override
    public List<StudentGroup> findByLecturerId(Long lecturerId) {
        return groups.values().stream()
                .filter(group -> group.getLecturer() != null && lecturerId.equals(group.getLecturer().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentGroup> findByStudentId(Long studentId) {
        return groups.values().stream()
                .filter(group -> group.getStudents().stream()
                        .anyMatch(student -> student.getId() != null && student.getId().equals(studentId)))
                .collect(Collectors.toList());
    }

    @Override
    public int countActiveStudentsByLecturerId(Long lecturerId) {
        Set<Long> studentIds = new HashSet<>();
        groups.values().stream()
                .filter(group -> group.getLecturer() != null && lecturerId.equals(group.getLecturer().getId()))
                .forEach(group -> group.getStudents().forEach(student -> {
                    if (student.getId() != null) {
                        studentIds.add(student.getId());
                    }
                }));
        return studentIds.size();
    }

    @Override
    public boolean existsById(Long groupId) {
        return groups.containsKey(groupId);
    }
}
