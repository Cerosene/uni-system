package pl.usos2.server.dao.schedule;
import pl.usos2.server.model.academic.ScheduleEntry;
import java.util.Map; 
import java.util.List;

public interface ScheduleDao {
    void saveEntry(Long groupId, String daySlot, String details);
    Map<String, String> getPlanByGroupId(Long groupId);
    void deleteEntry(Long groupId, String daySlot);
    List<ScheduleEntry> getScheduleForLecturer(Long lecturerId);

}