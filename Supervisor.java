public class Supervisor extends Person {
    public Supervisor(String name, String username, String password) {
        super(name, username, password);
    }
    // POLYMORPHISM: Supervisor's own version of the same methods
    @Override
    public String getRole() {
        return "SUPERVISOR";
    }
    @Override
    public boolean canViewReports() {
        return false;
    }
}
