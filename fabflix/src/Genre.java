public class Genre {

    private int id;
    private final String name;

    private boolean exists;
    public Genre(String name, int id) {
        this.name = name;
        this.id = id;
        this.exists = false;
    }

    public void setExists() {
        exists = true;
    }

    public boolean isExists() {
        return exists;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
