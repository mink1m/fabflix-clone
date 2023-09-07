public class User {

    private final String id;
    private String last_search;

    public User(String id) {
        this.id = id;
        this.last_search = "";
    }

    public String getId() {
        return this.id;
    }

    public String getLastSearch() {
        return this.last_search;
    }

    public void setLastSearch(String search) {
        this.last_search = search;
    }

}
