import java.sql.*;
import java.util.ArrayList;
public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/jandtexpress";
    private static final String USER = "root";
    private static final String PASS = "";
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    public static String[] login(String username, String password) throws SQLException {
        String sql = "SELECT full_name, role FROM users WHERE username=? AND password=SHA2(?,256)";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new String[]{rs.getString(1), rs.getString(2)};
            }
        }
        return null;
    }
    public static ArrayList<Main.Rider> loadRiders() throws SQLException {
        ArrayList<Main.Rider> list = new ArrayList<>();
        String sql = "SELECT r.rider_id, r.name, r.area, r.status, "
                + "COUNT(p.tracking_no) AS total, "
                + "COALESCE(SUM(CASE WHEN p.status='Delivered' THEN 1 ELSE 0 END),0) AS delivered, "
                + "COALESCE(SUM(CASE WHEN p.cod_amount>0 THEN 1 ELSE 0 END),0) AS cod, "
                + "COALESCE(SUM(CASE WHEN p.cod_amount>0 AND p.is_paid=1 THEN 1 ELSE 0 END),0) AS paid, "
                + "COALESCE(SUM(p.cod_amount),0) AS cod_amt, "
                + "COALESCE(SUM(CASE WHEN p.is_paid=1 THEN p.cod_amount ELSE 0 END),0) AS paid_amt, "
                + "COALESCE(SUM(CASE WHEN p.status='Delivered' THEN p.cod_amount ELSE 0 END),0) AS col_amt, "
                + "COALESCE(SUM(CASE WHEN p.status='Delivered' AND p.is_paid=1 THEN p.cod_amount ELSE 0 END),0) AS col_paid "
                + "FROM riders r LEFT JOIN parcels p ON p.rider_id = r.rider_id AND p.archived = 0 "
                + "GROUP BY r.rider_id, r.name, r.area, r.status ORDER BY r.rider_id";
        try (Connection c = get(); Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Main.Rider(rs.getInt("rider_id"),
                        rs.getString("name"), rs.getString("area"),
                        rs.getInt("total"), rs.getInt("delivered"),
                        rs.getInt("cod"), rs.getInt("paid"),
                        rs.getDouble("cod_amt"), rs.getDouble("paid_amt"),
                        rs.getDouble("col_amt"), rs.getDouble("col_paid"),
                        rs.getString("status")));
            }
        }
        return list;
    }
    public static Main.Rider insertRider(String name, String area) throws SQLException {
        String sql = "INSERT INTO riders (name,area) VALUES (?,?)";
        try (Connection c = get();
        PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, area);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                int id = k.next() ? k.getInt(1) : 0;
                return new Main.Rider(id, name, area, 0, 0, 0, 0, 0, 0, 0, 0, "Active");
            }
        }
    }
    public static void updateRider(int id, String name, String area, String status) throws SQLException {
        try (Connection c = get();
        PreparedStatement ps = c.prepareStatement(
                "UPDATE riders SET name=?, area=?, status=? WHERE rider_id=?")) {
            ps.setString(1, name);
            ps.setString(2, area);
            ps.setString(3, status);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }
    public static void setRiderStatus(int id, String status) throws SQLException {
        try (Connection c = get();
        PreparedStatement ps = c.prepareStatement("UPDATE riders SET status=? WHERE rider_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
    public static ArrayList<Main.Parcel> loadParcels() throws SQLException {
        ArrayList<Main.Parcel> list = new ArrayList<>();
        String sql = "SELECT p.tracking_no, p.sender, p.receiver, p.destination, p.status, "
                + "DATE_FORMAT(p.date_added,'%b %d, %Y') AS date_txt, p.archived, p.cod_amount, p.is_paid, "
                + "r.name AS rider_name "
                + "FROM parcels p LEFT JOIN riders r ON p.rider_id = r.rider_id "
                + "ORDER BY p.date_added DESC, p.tracking_no";
        try (Connection c = get(); Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Main.Parcel(rs.getString("tracking_no"), rs.getString("sender"),
                        rs.getString("receiver"), rs.getString("destination"),
                        rs.getString("status"), rs.getString("date_txt"),
                        rs.getString("rider_name") == null ? "Unassigned" : rs.getString("rider_name"),
                        rs.getInt("archived") == 1,
                        rs.getDouble("cod_amount"), rs.getInt("is_paid") == 1));
            }
        }
        return list;
    }
    public static void insertParcel(Main.Parcel p) throws SQLException {
        String sql = "INSERT INTO parcels (tracking_no,sender,receiver,destination,status,date_added,rider_id,cod_amount,is_paid,archived) "
                + "VALUES (?,?,?,?,?,STR_TO_DATE(?,'%b %d, %Y'),(SELECT rider_id FROM riders WHERE name=? LIMIT 1),?,?,0)";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.tr);
            ps.setString(2, p.sender);
            ps.setString(3, p.receiver);
            ps.setString(4, p.dest);
            ps.setString(5, p.status);
            ps.setString(6, p.date);
            ps.setString(7, p.rider);
            ps.setDouble(8, p.cod);
            ps.setInt(9, p.paid ? 1 : 0);
            ps.executeUpdate();
        }
    }
    public static void updateParcel(String tracking, String status, String rider, double cod, boolean paid) throws SQLException {
        String sql = "UPDATE parcels SET status=?, rider_id=(SELECT rider_id FROM riders WHERE name=? LIMIT 1), "
                + "cod_amount=?, is_paid=? WHERE tracking_no=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, rider);
            ps.setDouble(3, cod);
            ps.setInt(4, paid ? 1 : 0);
            ps.setString(5, tracking);
            ps.executeUpdate();
        }
    }
    public static void setParcelArchived(String tracking, boolean archived) throws SQLException {
        try (Connection c = get();
        PreparedStatement ps = c.prepareStatement("UPDATE parcels SET archived=? WHERE tracking_no=?")) {
            ps.setInt(1, archived ? 1 : 0);
            ps.setString(2, tracking);
            ps.executeUpdate();
        }
    }
    public static int countByStatus(String status) throws SQLException {
        String sql = status == null ? "SELECT COUNT(*) FROM parcels WHERE archived=0"
                : "SELECT COUNT(*) FROM parcels WHERE archived=0 AND status=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    public static ArrayList<Main.Ticket> loadTickets() throws SQLException {
        ArrayList<Main.Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.tracking_no, p.receiver, t.concern, t.status, t.archived, "
                + "u.full_name AS closed_name "
                + "FROM tickets t JOIN parcels p ON t.tracking_no = p.tracking_no "
                + "LEFT JOIN users u ON t.closed_by_user = u.username ORDER BY t.ticket_id";
        try (Connection c = get(); Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Main.Ticket(rs.getString("ticket_id"), rs.getString("tracking_no"),
                        rs.getString("receiver"), rs.getString("concern"), rs.getString("status"),
                        rs.getString("closed_name") == null ? "-" : rs.getString("closed_name"),
                        rs.getInt("archived") == 1));
            }
        }
        return list;
    }
    public static String nextTicketId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(ticket_id,2) AS UNSIGNED)),0) FROM tickets";
        try (Connection c = get(); Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return String.format("T%03d", rs.getInt(1) + 1);
        }
    }
    public static void insertTicket(String id, String trackingNo, String concern) throws SQLException {
        String sql = "INSERT INTO tickets (ticket_id,tracking_no,concern,status,archived) VALUES (?,?,?,'Open',0)";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, trackingNo);
            ps.setString(3, concern);
            ps.executeUpdate();
        }
    }
    public static void closeTicket(String id, String closedByUsername) throws SQLException {
        String sql = "UPDATE tickets SET status='Closed', closed_by_user=? WHERE ticket_id=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, closedByUsername);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }
    public static void setTicketArchived(String id, boolean archived) throws SQLException {
        try (Connection c = get();
        PreparedStatement ps = c.prepareStatement("UPDATE tickets SET archived=? WHERE ticket_id=?")) {
            ps.setInt(1, archived ? 1 : 0);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }
    /** Each row = {tracking_no, date_time, location, status, remarks}. Pass null for all parcels. */
    public static ArrayList<String[]> loadHistory(String trackingNo) throws SQLException {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT tracking_no, DATE_FORMAT(date_time,'%b %d, %Y %h:%i %p') AS dt, location, status, remarks "
                + "FROM tracking_history "
                + (trackingNo == null ? "" : "WHERE tracking_no=? ")
                + "ORDER BY tracking_history.date_time DESC, history_id DESC";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (trackingNo != null) ps.setString(1, trackingNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString("tracking_no"), rs.getString("dt"),
                                rs.getString("location"), rs.getString("status"),
                                rs.getString("remarks") == null ? "" : rs.getString("remarks")});
                }
            }
        }
        return list;
    }
    public static void addHistory(String trackingNo, String location, String status, String remarks) throws SQLException {
        String sql = "INSERT INTO tracking_history (tracking_no,date_time,location,status,remarks) VALUES (?,NOW(),?,?,?)";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, trackingNo);
            ps.setString(2, location);
            ps.setString(3, status);
            ps.setString(4, remarks);
            ps.executeUpdate();
        }
    }
}
