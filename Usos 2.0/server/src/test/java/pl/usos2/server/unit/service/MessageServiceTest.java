package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
                1L,
                "Anna",
                "Nowak",
                "anna@test.pl",
                "haslo123",
                "s11111",
                "Informatyka",
                Semester.THIRD
        );

        lecturer = new Lecturer(
                2L,
                "Adam",
                "Profesor",
                "adam@test.pl",
                "haslo123",
                "P0001",
                "dr"
        );
    }

    @Test
    @DisplayName("Powinno wysłać wiadomość z wygenerowanym id")
    void shouldSendMessageWithGeneratedId() {
        System.out.println("Test: wysyłanie wiadomości z generowanym id");

        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Czy termin jest aktualny?");

        assertNotNull(message.getId());
        assertEquals(MessageStatus.SENT, message.getStatus());
    }

    @Test
    @DisplayName("Powinno zablokować wysłanie wiadomości do samego siebie")
    void shouldThrowExceptionWhenSenderAndRecipientAreTheSame() {
        System.out.println("Test: blokada wysłania wiadomości do samego siebie");

        assertThrows(IllegalArgumentException.class,
                () -> messageService.sendMessage(student, student, "Test", "Treść"));
    }

    @Test
    @DisplayName("Powinno zwrócić inbox odbiorcy")
    void shouldReturnInbox() {
        System.out.println("Test: pobieranie inbox odbiorcy");

        messageService.sendMessage(student, lecturer, "Pytanie", "Treść");

        assertEquals(1, messageService.getInbox(lecturer).size());
    }

    @Test
    @DisplayName("Powinno zwrócić nieprzeczytane wiadomości odbiorcy")
    void shouldReturnUnreadInbox() {
        System.out.println("Test: pobieranie nieprzeczytanych wiadomości");

        messageService.sendMessage(student, lecturer, "Pytanie", "Treść");

        assertEquals(1, messageService.getUnreadInbox(lecturer).size());
    }

    @Test
    @DisplayName("Powinno oznaczyć wiadomość jako przeczytaną")
    void shouldMarkMessageAsRead() {
        System.out.println("Test: oznaczanie wiadomości jako przeczytanej");

        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Treść");
        messageService.markAsRead(message, lecturer);

        assertEquals(MessageStatus.READ, message.getStatus());
        assertEquals(0, messageService.getUnreadInbox(lecturer).size());
    }

    @Test
    @DisplayName("Powinno zablokować oznaczenie wiadomości jako przeczytanej przez złą osobę")
    void shouldThrowExceptionWhenWrongUserMarksMessageAsRead() {
        System.out.println("Test: tylko odbiorca może oznaczyć wiadomość jako przeczytaną");

        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Treść");

        assertThrows(IllegalArgumentException.class,
                () -> messageService.markAsRead(message, student));
    }

    @Test
    @DisplayName("Powinno zablokować ponowne oznaczenie wiadomości jako przeczytanej")
    void shouldThrowExceptionWhenMessageAlreadyRead() {
        System.out.println("Test: blokada ponownego oznaczenia wiadomości jako przeczytanej");

        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Treść");
        messageService.markAsRead(message, lecturer);

        assertThrows(IllegalStateException.class,
                () -> messageService.markAsRead(message, lecturer));
    }

    @Test
    @DisplayName("Powinno znaleźć wiadomość po id")
    void shouldFindMessageById() {
        System.out.println("Test: wyszukiwanie wiadomości po id");

        Message message = messageService.sendMessage(student, lecturer, "Pytanie", "Treść");

        assertEquals(message.getId(), messageService.findById(message.getId()).getId());
    }

    @Test
    @DisplayName("Powinno zwrócić wiadomości wysłane przez nadawcę")
    void shouldReturnSentMessages() {
        System.out.println("Test: pobieranie wysłanych wiadomości");

        messageService.sendMessage(student, lecturer, "Pytanie", "Treść");

        assertEquals(1, messageService.getSentMessages(student).size());
    }
}