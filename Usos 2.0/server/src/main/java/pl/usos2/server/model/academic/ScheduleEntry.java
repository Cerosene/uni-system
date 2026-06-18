package pl.usos2.server.model.academic;

public class ScheduleEntry {
    private final int day;
    private final int slot;
    private final String subjectName;

    public ScheduleEntry(int day, int slot, String subjectName) {
        this.day = day;
        this.slot = slot;
        this.subjectName = subjectName;
    }

    public int getDay() { return day; }
    public int getSlot() { return slot; }
    public String getSubjectName() { return subjectName; }
}