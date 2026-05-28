package pl.usos2.client.util;

import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;

public final class DisplayTextFormatter {

    private DisplayTextFormatter() {
    }

    public static String formatRequestType(RequestType type) {
        if (type == null) {
            return "";
        }

        boolean isEn = isEnglish();
        return switch (type) {
            case SCHOLARSHIP -> isEn ? "Scholarship" : "Stypendium";
            case LEAVE -> isEn ? "Leave" : "Urlop";
            case CERTIFICATE -> isEn ? "Certificate" : "Zaświadczenie";
            case OTHER -> isEn ? "Other" : "Inne";
        };
    }

    public static String formatRequestStatus(RequestStatus status) {
        if (status == null) {
            return "";
        }

        boolean isEn = isEnglish();
        return switch (status) {
            case SUBMITTED -> isEn ? "Submitted" : "Złożony";
            case IN_REVIEW -> isEn ? "In review" : "W trakcie weryfikacji";
            case APPROVED -> isEn ? "Approved" : "Zatwierdzony";
            case REJECTED -> isEn ? "Rejected" : "Odrzucony";
        };
    }

    public static String formatServiceTicketStatus(ServiceTicketStatus status) {
        if (status == null) {
            return "";
        }

        boolean isEn = isEnglish();
        return switch (status) {
            case OPEN -> isEn ? "Open" : "Otwarty";
            case IN_PROGRESS -> isEn ? "In progress" : "W trakcie";
            case CLOSED -> isEn ? "Closed" : "Zamknięty";
        };
    }

    private static boolean isEnglish() {
        return "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
    }
}
