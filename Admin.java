public class Admin extends Person {
    public Admin(String name, String username, String password) {
        super(name, username, password);
    }
    // POLYMORPHISM: Admin's own version of the abstract methods
    @Override
    public String getRole() {
        return "ADMIN";
    }
    @Override
    public boolean canViewReports() {
        return true;
    }
}
