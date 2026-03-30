package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.message.MessageService;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    private MessageService messageService;
    private Student student;
    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
        student = new Student(
                1L, "Anna", "Nowak", "anna@test.pl", "haslo123", "s11111", "Informatyka", Semester.THIRD
        );
        lecturer = new Lecturer(
                2L, "Adam", "Profesor", "adam@test.pl", "haslo123", "p0001", "dr"
        );
    }

    @Test
    void shouldSendMessageCorrectly() {
        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Czy termin jest aktualny?");

        assertNotNull(message);
        assertEquals(MessageStatus.SENT, message.getStatus());
        assertEquals("Pytanie", message.getSubject());
    }

    @Test
    void shouldThrowExceptionWhenSenderAndRecipientAreTheSame() {
        assertThrows(IllegalArgumentException.class,
                () -> messageService.sendMessage(student, student, "Test", "Treść"));
    }

    @Test
    void shouldThrowExceptionWhenSubjectIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> messageService.sendMessage(student, lecturer, " ", "Treść"));
    }
}