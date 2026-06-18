package pl.usos2.server.service.schedule;

import pl.usos2.server.dao.schedule.ScheduleDao;
import java.util.Map;
import pl.usos2.server.dao.schedule.JdbcScheduleDao;
import pl.usos2.server.model.academic.ScheduleEntry;
import java.util.List;
import java.util.HashMap;

public class ScheduleService {
    private final ScheduleDao scheduleDao;

    public ScheduleService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public void saveEntry(Long groupId, String daySlot, String details) {
        scheduleDao.saveEntry(groupId, daySlot, details);
    }

    public Map<String, String> getSchedule(Long groupId) {
        return scheduleDao.getPlanByGroupId(groupId);
    }

    public void deleteEntry(Long groupId, String daySlot) {
        scheduleDao.deleteEntry(groupId, daySlot);
    }

   public Map<String, String> getScheduleForLecturer(Long lecturerId) {
        List<ScheduleEntry> entries = scheduleDao.getScheduleForLecturer(lecturerId);
        
        Map<String, String> result = new HashMap<>();
        for (ScheduleEntry entry : entries) {
            String key = entry.getDay() + "_" + entry.getSlot();
            result.put(key, entry.getSubjectName());
        }
        return result;
    }
    
}