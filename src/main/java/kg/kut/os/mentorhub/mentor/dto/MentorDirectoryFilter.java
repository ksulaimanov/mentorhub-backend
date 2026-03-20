package kg.kut.os.mentorhub.mentor.dto;

public class MentorDirectoryFilter {

    private String query;
    private String specialization;
    private String city;
    private Boolean online;
    private Boolean offline;
    private Boolean hybrid;
    private String sortBy;

    public MentorDirectoryFilter() {
    }

    public String getQuery() {
        return query;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getCity() {
        return city;
    }

    public Boolean getOnline() {
        return online;
    }

    public Boolean getOffline() {
        return offline;
    }

    public Boolean getHybrid() {
        return hybrid;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public void setHybrid(Boolean hybrid) {
        this.hybrid = hybrid;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}