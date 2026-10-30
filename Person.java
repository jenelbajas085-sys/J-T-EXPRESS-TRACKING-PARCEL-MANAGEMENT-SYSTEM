public abstract class Person {
    // ENCAPSULATION: private fields, accessed only through public methods
    private String name;
    private String username;
    private String password;
    public Person(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
    public String getName() {
        return name;
    }
    public String getUsername() {
        return username;
    }
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
    // Abstract methods: every child class MUST provide its own version
    public abstract String getRole();
    public abstract boolean canViewReports();
}
