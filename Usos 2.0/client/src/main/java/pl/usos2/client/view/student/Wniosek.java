package pl.usos2.client.view.student;

public class Wniosek {
    private final String typ;
    private final String tresc;
    private final String dataZlozenia;
    private final String status;

    private Wniosek(WniosekBuilder builder) {
        this.typ = builder.typ;
        this.tresc = builder.tresc;
        this.dataZlozenia = builder.dataZlozenia;
        this.status = "Oczekujący";
    }

    public static class WniosekBuilder {
        private String typ;
        private String tresc;
        private String dataZlozenia;

        public WniosekBuilder setTyp(String typ) { this.typ = typ; return this; }
        public WniosekBuilder setTresc(String tresc) { this.tresc = tresc; return this; }
        public WniosekBuilder setData(String data) { this.dataZlozenia = data; return this; }
        public Wniosek build() { return new Wniosek(this); }
    }
}