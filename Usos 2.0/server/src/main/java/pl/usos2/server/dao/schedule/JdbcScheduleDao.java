package pl.usos2.server.dao.schedule;

import pl.usos2.server.database.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import pl.usos2.server.model.academic.ScheduleEntry;


public class JdbcScheduleDao implements ScheduleDao {
    private static final Logger logger = Logger.getLogger(JdbcScheduleDao.class.getName());

    private static final String SAVE_SQL = """
            MERGE INTO schedule t
            USING (SELECT ? AS gid, ? AS ds FROM DUAL) s
            ON (t.group_id = s.gid AND t.day_slot = s.ds)
            WHEN MATCHED THEN UPDATE SET t.details = ?
            WHEN NOT MATCHED THEN INSERT (group_id, day_slot, details) VALUES (?, ?, ?)
            """;

    private static final String SELECT_BY_GROUP_SQL = "SELECT day_slot, details FROM schedule WHERE group_id = ?";
    private static final String DELETE_SQL = "DELETE FROM schedule WHERE group_id = ? AND day_slot = ?";

    @Override
    public void saveEntry(Long groupId, String daySlot, String details) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {
          
            statement.setLong(1, groupId);
            statement.setString(2, daySlot);
            statement.setString(3, details);
            statement.setLong(4, groupId);
            statement.setString(5, daySlot);
            statement.setString(6, details);
            
            statement.executeUpdate();
            logger.info("Schedule entry saved for group " + groupId + " at " + daySlot);
        } catch (SQLException e) {
            throw new IllegalStateException("Error saving schedule entry", e);
        }
    }

    @Override
    public Map<String, String> getPlanByGroupId(Long groupId) {
        Map<String, String> plan = new HashMap<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_GROUP_SQL)) {
            
            statement.setLong(1, groupId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    plan.put(rs.getString("day_slot"), rs.getString("details"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error fetching schedule for group " + groupId, e);
        }
        return plan;
    }

    @Override
    public void deleteEntry(Long groupId, String daySlot) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            
            statement.setLong(1, groupId);
            statement.setString(2, daySlot);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error deleting schedule entry", e);
        }
    }

 public List<ScheduleEntry> getScheduleForLecturer(Long lecturerId) {
    String sql = """
        SELECT s.day_slot, MIN(sub.subject_name) as subject_name 
        FROM USOS.SCHEDULE s
        JOIN USOS.COURSE_GROUPS cg ON s.group_id = cg.group_id
        JOIN USOS.SUBJECTS sub ON cg.subject_id = sub.subject_id
        WHERE sub.lecturer_id = ?
        GROUP BY s.day_slot
    """;

    List<ScheduleEntry> entries = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        
        statement.setLong(1, lecturerId);
        
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String daySlot = rs.getString("day_slot");
                String subjectName = rs.getString("subject_name");
                
              
                if (daySlot != null && daySlot.contains("_")) {
                    String[] parts = daySlot.split("_");
                    try {
                        int day = Integer.parseInt(parts[0]);
                        int slot = Integer.parseInt(parts[1]);
                      
                        entries.add(new ScheduleEntry(day, slot, subjectName));
                        
                       
                    } catch (NumberFormatException e) {
                        System.err.println("Błąd parsowania day_slot:" + daySlot);
                    }
                }
            }
        }
    } catch (SQLException e) {
        throw new IllegalStateException("Error fetching schedule for lecturer " + lecturerId, e);
    }
    return entries;
}
}