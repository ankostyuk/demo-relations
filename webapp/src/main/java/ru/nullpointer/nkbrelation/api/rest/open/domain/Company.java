package ru.nullpointer.nkbrelation.api.rest.open.domain;

/**
 * @author Maksim Konyuhov
 * @author ankostyuk
 */
public class Company extends PublicNode {

    private String inn;
    private String ogrn;
    private String okpo;

    private String address;
    private String chiefName;
    private String fullName;
    private String shortName;

    private String kpp;
    private String regDate;
    private Status status;

    private String okopf;
    private String okved;
    private String okato;

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getOkpo() {
        return okpo;
    }

    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getChiefName() {
        return chiefName;
    }

    public void setChiefName(String chiefName) {
        this.chiefName = chiefName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOkopf() {
        return okopf;
    }

    public void setOkopf(String okopf) {
        this.okopf = okopf;
    }

    public String getOkved() {
        return okved;
    }

    public void setOkved(String okved) {
        this.okved = okved;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    @Override
    public String toString() {
        return "Company{"
                + "id=" + id
                + ", inn=" + inn
                + ", ogrn=" + ogrn
                + ", okpo=" + okpo
                + ", address=" + address
                + ", chiefName=" + chiefName
                + ", fullName=" + fullName
                + ", shortName=" + shortName
                + ", kpp=" + kpp
                + ", regDate=" + regDate
                + ", status=" + status
                + ", okopf=" + okopf
                + ", okved=" + okved
                + ", okato=" + okato
                + ", relations=" + relations
                + '}';
    }

    public static class Status {

        private String type;
        private String date;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "Status{"
                    + "type=" + type
                    + ", date=" + date
                    + '}';
        }
    }
}
