public class Actor {

    private final String name;
    private final int birthYear;

    private String id;

    private boolean exists;

    public Actor(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
        this.id = null;
        this.exists = false;
    }

    public String getName() {
        return name;
    }
    public int getBirthYear() {
        return birthYear;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public boolean isExists() {
        return exists;
    }

    public void markExsits() {
        exists = true;
    }

    public String toString() {
        return "name, " + getName() +
                ", DOB, " + getBirthYear() +
                ", ID, " + getId();
    }
}
