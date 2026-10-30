import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;
public class Main extends JFrame {
    private final Color RED = new Color(220, 0, 0),
            DARK = new Color(30, 37, 41),
            BG = new Color(246, 246, 246),
            BORDER = new Color(190, 190, 190);
    private final String[] STATUS = {"Pending", "On the Way", "Delivered"};
    private final String[] STATUS_ALL = {"All", "Pending", "On the Way", "Delivered"};
    private static final String HUB = "Cagayan de Oro City";
    private static final int MAX_PARCELS = 150;
    private JPanel content;
    private JLabel title;
    private String loggedUser = "", role = "";
    private Person currentUser;
    private final ArrayList<Rider> riders = new ArrayList<>();
    private final ArrayList<Parcel> parcels = new ArrayList<>();
    private final ArrayList<Ticket> tickets = new ArrayList<>();
    private ArrayList<Parcel> parcelRows = new ArrayList<>();
    private ArrayList<Ticket> ticketRows = new ArrayList<>();
    private boolean viewParcelArchive = false, viewTicketArchive = false;
    public Main() {
        setTitle("J&T Express Parcel Tracking Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        loadData();
        login();
    }
    void dbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error",
                JOptionPane.WARNING_MESSAGE);
    }
    boolean confirm(String msg, String ttl) {
        return JOptionPane.showConfirmDialog(this, msg, ttl,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    boolean form(JPanel p, String ttl) {
        return JOptionPane.showConfirmDialog(this, p, ttl,
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }
    double readCod(JTextField f) {
        try {
            String s = f.getText().trim();
            double amt = Double.parseDouble(s.isEmpty() ? "0" : s);
            if (amt < 0) throw new NumberFormatException();
            return amt;
        } catch (NumberFormatException ex) {
            warn("COD amount must be a number (0 or more).");
            return -1;
        }
    }
    void loadData() {
        try {
            riders.clear();
            parcels.clear();
            tickets.clear();
            riders.addAll(DB.loadRiders());
            parcels.addAll(DB.loadParcels());
            tickets.addAll(DB.loadTickets());
        } catch (SQLException ex) {
            dbError(ex);
        }
    }
    void login() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(600, 540));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel brand = lbl("J&T EXPRESS", 38, RED);
        brand.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 38));
        brand.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = lbl("— Express Your Online Business —", 15, Color.DARK_GRAY);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sys = lbl("<html><center>Parcel Tracking<br>Management System</center></html>", 24, Color.BLACK);
        sys.setAlignmentX(CENTER_ALIGNMENT);
        left.add(Box.createVerticalGlue());
        left.add(brand);
        left.add(sub);
        left.add(Box.createVerticalStrut(8));
        left.add(sys);
        left.add(Box.createVerticalStrut(10));
        left.add(new Truck());
        left.add(Box.createVerticalGlue());
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(360, 340));
        box.setBackground(Color.WHITE);
        box.setBorder(new CompoundBorder(
                new LineBorder(new Color(140, 140, 140)),
                new EmptyBorder(25, 30, 25, 30)));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel lt = lbl("LOGIN", 25, new Color(120, 30, 30));
        lt.setFont(new Font("Arial", Font.BOLD, 25));
        lt.setAlignmentX(CENTER_ALIGNMENT);
        box.add(lt);
        box.add(Box.createVerticalStrut(25));
        JLabel usernameLabel = label("Username");
        usernameLabel.setAlignmentX(CENTER_ALIGNMENT);
        box.add(usernameLabel);
        box.add(Box.createVerticalStrut(5));
        JTextField u = new JTextField();
        input(u);
        u.setAlignmentX(CENTER_ALIGNMENT);
        box.add(u);
        box.add(Box.createVerticalStrut(15));
        JLabel passwordLabel = label("Password");
        passwordLabel.setAlignmentX(CENTER_ALIGNMENT);
        box.add(passwordLabel);
        box.add(Box.createVerticalStrut(5));
        JPasswordField p = new JPasswordField();
        input(p);
        p.setAlignmentX(CENTER_ALIGNMENT);
        box.add(p);
        box.add(Box.createVerticalStrut(22));
        JButton b = red("LOGIN");
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        box.add(b);
        b.addActionListener(e -> {
            String un = u.getText().trim().toLowerCase();
            String pw = new String(p.getPassword());
            try {
                String[] r = DB.login(un, pw);
                if (r != null) {
                    loggedUser = r[0];
                    currentUser = r[1].equals("ADMIN")
                            ? new Admin(r[0], un, pw)
                            : new Supervisor(r[0], un, pw);
                    role = currentUser.getRole();
                    viewParcelArchive = false;
                    viewTicketArchive = false;
                    loadData();
                    dashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                dbError(ex);
            }
        });
        p.addActionListener(e -> b.doClick());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 18, 10, 18);
        g.gridy = 0;
        g.gridx = 0;
        add(left, g);
        g.gridx = 1;
        add(box, g);
        revalidate();
        repaint();
    }
    JLabel lbl(String s, int size, Color c) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Arial", Font.PLAIN, size));
        l.setForeground(c);
        return l;
    }
    JLabel label(String s) {
        JLabel l = lbl(s, 13, Color.BLACK);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        return l;
    }
    void input(JTextField f) {
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(150, 150, 150)),
                new EmptyBorder(7, 9, 7, 9)));
    }
    JButton red(String s) {
        JButton b = new JButton(s);
        b.setBackground(RED);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 15, 9, 15));
        return b;
    }
    void style(JTable t) {
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.setRowHeight(34);
        t.setGridColor(new Color(205, 205, 205));
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(238, 238, 238));
    }
    String peso(double x) {
        return String.format("₱%,.2f", x);
    }
    JTable readOnlyTable(Object[][] d, String[] c) {
        JTable t = new JTable(new DefaultTableModel(d, c) {
            public boolean isCellEditable(int r, int col) {
                return false;
            }
        });
        style(t);
        return t;
    }
    void shell(String page) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(RED);
        head.setPreferredSize(new Dimension(100, 55));
        JLabel logo = lbl(" J&T EXPRESS", 24, Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
        head.add(logo, BorderLayout.WEST);
        JLabel user = lbl("● " + loggedUser + " (" + role + ") ", 14, Color.WHITE);
        user.setFont(new Font("Arial", Font.BOLD, 14));
        head.add(user, BorderLayout.EAST);
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(205, 0));
        side.setBackground(DARK);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        String[] menu = {
            "⌂ Dashboard",
                    "▣ Parcel Management",
                    "⌕ Track Parcel",
                    "◷ Tracking History",
                    "▥ Reports",
                    "♟ Riders",
                    "▰ Tickets / Concerns",
                    "⇥ Logout"};
        for (String m : menu) {
            JButton x = new JButton(m);
            x.setHorizontalAlignment(SwingConstants.LEFT);
            x.setForeground(Color.WHITE);
            x.setBackground(DARK);
            x.setFont(new Font("Arial", Font.BOLD, 12));
            x.setBorder(new EmptyBorder(13, 14, 13, 4));
            x.setFocusPainted(false);
            x.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            if (m.contains("Dashboard"))
                    x.addActionListener(e -> dashboard());
            else if (m.contains("Parcel Management"))
                    x.addActionListener(e -> parcelManagement());
            else if (m.contains("Track Parcel"))
                    x.addActionListener(e -> track());
            else if (m.contains("Tracking History"))
                    x.addActionListener(e -> history());
            else if (m.contains("Reports"))
                    x.addActionListener(e -> reports());
            else if (m.contains("Riders"))
                    x.addActionListener(e -> riders());
            else if (m.contains("Tickets"))
                    x.addActionListener(e -> tickets());
            else
                    x.addActionListener(e -> {
                if (confirm("Logout?", "Logout")) {
                    loggedUser = "";
                    role = "";
                    currentUser = null;
                    login();
                }
            });
            side.add(x);
        }
        side.add(Box.createVerticalGlue());
        side.add(lbl("Role: " + role, 12, Color.LIGHT_GRAY));
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        title = lbl(page, 23, Color.BLACK);
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setBorder(new EmptyBorder(16, 20, 10, 10));
        content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        center.add(title, BorderLayout.NORTH);
        center.add(content, BorderLayout.CENTER);
        add(head, BorderLayout.NORTH);
        add(side, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
    }
    void setPage(String s, JComponent c) {
        title.setText(s);
        content.removeAll();
        content.add(c, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
    JPanel pageRoot(int gap) {
        JPanel root = new JPanel(new BorderLayout(gap, gap));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(0, 18, 18, 18));
        return root;
    }
    JPanel box(String t) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(BORDER), t,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)),
                new EmptyBorder(8, 8, 8, 8)));
        return p;
    }
    JPanel filterBar(JTable t, String catLabel, String[] cats, int catCol, int searchWidth) {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        f.setOpaque(false);
        f.add(label("Search:"));
        JTextField q = new JTextField(searchWidth);
        f.add(q);
        JComboBox<String> cb = null;
        if (cats != null) {
            f.add(label(catLabel + ":"));
            cb = new JComboBox<>(cats);
            f.add(cb);
        }
        final JComboBox<String> cat = cb;
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(t.getModel());
        t.setRowSorter(sorter);
        Runnable apply = () -> {
            ArrayList<RowFilter<Object, Object>> fl = new ArrayList<>();
            String s = q.getText().trim();
            if (!s.isEmpty())
                    fl.add(RowFilter.regexFilter("(?i)" + Pattern.quote(s)));
            if (cat != null && cat.getSelectedIndex() > 0)
                    fl.add(RowFilter.regexFilter("^" + Pattern.quote((String) cat.getSelectedItem()) + "$", catCol));
            sorter.setRowFilter(fl.isEmpty() ? null : RowFilter.<Object, Object>andFilter(fl));
        };
        q.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { apply.run(); }
            public void removeUpdate(DocumentEvent e) { apply.run(); }
            public void changedUpdate(DocumentEvent e) { apply.run(); }
        });
        if (cat != null)
                cat.addActionListener(e -> apply.run());
        return f;
    }
    JPanel northBar(JPanel bar, JPanel top) {
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(bar, BorderLayout.WEST);
        north.add(top, BorderLayout.EAST);
        return north;
    }
    JPanel rightBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        return top;
    }
    void dashboard() {
        String welcome = "DASHBOARD — Welcome, " + loggedUser + " (" + role + ")";
        shell(welcome);
        JPanel root = pageRoot(10);
        int tot = 0, pen = 0, tra = 0, del = 0;
        try {
            tot = DB.countByStatus(null);
            pen = DB.countByStatus("Pending");
            tra = DB.countByStatus("On the Way");
            del = DB.countByStatus("Delivered");
        } catch (SQLException ex) {
            dbError(ex);
        }
        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setOpaque(false);
        cards.add(card("TOTAL PARCELS", "" + tot, "▣"));
        cards.add(card("PENDING", "" + pen, "◷"));
        cards.add(card("ON THE WAY", "" + tra, "▱"));
        cards.add(card("DELIVERED", "" + del, "✓"));
        JPanel low = new JPanel(new GridLayout(1, 2, 10, 0));
        low.setOpaque(false);
        JTable rt = table(false);
        JPanel recent = box("RECENT PARCELS");
        recent.add(filterBar(rt, "Status", STATUS_ALL, 2, 8), BorderLayout.NORTH);
        recent.add(new JScrollPane(rt), BorderLayout.CENTER);
        JPanel chart = box("PARCEL STATUS");
        chart.add(new Chart(pen, tra, del), BorderLayout.CENTER);
        low.add(recent);
        low.add(chart);
        root.add(cards, BorderLayout.NORTH);
        root.add(low, BorderLayout.CENTER);
        setPage(welcome, root);
    }
    JPanel card(String n, String v, String i) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(12, 13, 12, 13)));
        p.add(lbl(n, 12, Color.BLACK), BorderLayout.NORTH);
        p.add(lbl(v, 28, Color.BLACK), BorderLayout.CENTER);
        p.add(lbl(i, 27, RED), BorderLayout.EAST);
        return p;
    }
    void parcelManagement() {
        shell("PARCEL MANAGEMENT");
        JPanel root = pageRoot(8);
        JTable t = table(true);
        JPanel bar = filterBar(t, "Status", STATUS_ALL, 5, 16);
        JPanel top = rightBar();
        JComboBox<String> view = new JComboBox<>(new String[]{"Active Parcels", "Archived Parcels"});
        view.setSelectedIndex(viewParcelArchive ? 1 : 0);
        view.addActionListener(e -> {
            viewParcelArchive = view.getSelectedIndex() == 1;
            parcelManagement();
        });
        top.add(view);
        if (!viewParcelArchive) {
            JButton add = red("+ ADD PARCEL");
            add.addActionListener(e -> addParcel());
            top.add(add);
        }
        root.add(northBar(bar, top), BorderLayout.NORTH);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        setPage("PARCEL MANAGEMENT", root);
    }
    JTable table(boolean action) {
        ArrayList<Parcel> rows = new ArrayList<>();
        for (Parcel p : parcels) {
            if (action ? p.archived == viewParcelArchive : !p.archived)
                    rows.add(p);
        }
        if (action)
                parcelRows = rows;
        String[] c = action
                ? new String[]{"Tracking Number", "Sender", "Receiver", "Destination", "Rider",
                    "Status", "COD", "Action"}
        : new String[]{"Tracking Number", "Receiver", "Status", "Date"};
        Object[][] d = new Object[rows.size()][c.length];
        for (int i = 0; i < rows.size(); i++) {
            Parcel p = rows.get(i);
            d[i] = action
                    ? new Object[]{p.tr, p.sender, p.receiver, p.dest, p.rider, p.status, codText(p),
                        viewParcelArchive ? "RESTORE" : "EDIT / ARCHIVE"}
            : new Object[]{p.tr, p.receiver, p.status, p.date};
        }
        JTable t = new JTable(new DefaultTableModel(d, c) {
            public boolean isCellEditable(int r, int col) {
                return action && col == 7;
            }
        });
        style(t);
        if (action) {
            t.getColumnModel().getColumn(7).setCellRenderer(new BtnRenderer());
            t.getColumnModel().getColumn(7).setCellEditor(new ParcelEditor(new JCheckBox(), t));
        }
        return t;
    }
    JComboBox<String> riderCombo(String current) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem("Unassigned");
        for (Rider r : riders)
                if (r.status.equals("Active"))
                cb.addItem(r.name);
        if (current != null) {
            boolean found = false;
            for (int i = 0; i < cb.getItemCount(); i++)
                    if (cb.getItemAt(i).equals(current))
                    found = true;
            if (!found)
                    cb.addItem(current);
            cb.setSelectedItem(current);
        }
        return cb;
    }
    String codText(Parcel p) {
        return p.cod > 0 ? peso(p.cod) + (p.paid ? " (Paid)" : " (Unpaid)") : "-";
    }
    void addParcel() {
        JTextField tr = new JTextField("JT");
        JTextField s = new JTextField();
        JTextField r = new JTextField();
        JTextField d = new JTextField();
        JComboBox<String> st = new JComboBox<>(STATUS);
        JComboBox<String> rd = riderCombo("Unassigned");
        JTextField cod = new JTextField("0");
        JCheckBox paid = new JCheckBox("COD already paid");
        JPanel p = new JPanel(new GridLayout(0, 2, 7, 7));
        p.add(new JLabel("Tracking Number"));
        p.add(tr);
        p.add(new JLabel("Sender"));
        p.add(s);
        p.add(new JLabel("Receiver"));
        p.add(r);
        p.add(new JLabel("Destination"));
        p.add(d);
        p.add(new JLabel("Status"));
        p.add(st);
        p.add(new JLabel("Rider"));
        p.add(rd);
        p.add(new JLabel("COD Amount (0 if none)"));
        p.add(cod);
        p.add(new JLabel(""));
        p.add(paid);
        if (form(p, "Add Parcel")) {
            if (tr.getText().trim().length() <= 2 || s.getText().trim().isEmpty()
                    || r.getText().trim().isEmpty() || d.getText().trim().isEmpty()) {
                warn("Please fill in all fields.");
                return;
            }
            double amt = readCod(cod);
            if (amt < 0) return;
            String today = new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date());
            Parcel np = new Parcel(tr.getText().trim(), s.getText().trim(),
                    r.getText().trim(), d.getText().trim(),
                    (String) st.getSelectedItem(), today,
                    (String) rd.getSelectedItem(), false, amt, paid.isSelected() && amt > 0);
            try {
                DB.insertParcel(np);
                DB.addHistory(np.tr, HUB, np.status, "Parcel added to the system");
                parcels.add(0, np);
            } catch (SQLException ex) {
                dbError(ex);
            }
            parcelManagement();
        }
    }
    void editParcel(Parcel p) {
        JComboBox<String> st = new JComboBox<>(STATUS);
        st.setSelectedItem(p.status);
        JComboBox<String> rd = riderCombo(p.rider);
        JTextField cod = new JTextField(p.cod == 0 ? "0" : String.valueOf(p.cod));
        JCheckBox paid = new JCheckBox("COD already paid", p.paid);
        JPanel pn = new JPanel(new GridLayout(0, 2, 7, 7));
        pn.add(new JLabel("Tracking Number"));
        pn.add(new JLabel(p.tr));
        pn.add(new JLabel("Status"));
        pn.add(st);
        pn.add(new JLabel("Rider"));
        pn.add(rd);
        pn.add(new JLabel("COD Amount (0 if none)"));
        pn.add(cod);
        pn.add(new JLabel(""));
        pn.add(paid);
        if (form(pn, "Edit Parcel")) {
            double amt = readCod(cod);
            if (amt < 0) return;
            String ns = (String) st.getSelectedItem();
            String nr = (String) rd.getSelectedItem();
            boolean np = paid.isSelected() && amt > 0;
            try {
                DB.updateParcel(p.tr, ns, nr, amt, np);
                if (!ns.equals(p.status))
                        DB.addHistory(p.tr, ns.equals("Delivered") ? p.dest : HUB, ns, "Status changed to " + ns);
                p.status = ns;
                p.rider = nr;
                p.cod = amt;
                p.paid = np;
            } catch (SQLException ex) {
                dbError(ex);
            }
        }
    }
    void archiveParcel(Parcel p, boolean flag) {
        try {
            DB.setParcelArchived(p.tr, flag);
            p.archived = flag;
        } catch (SQLException ex) {
            dbError(ex);
        }
    }
    void track() {
        shell("TRACK PARCEL");
        JPanel root = pageRoot(10);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.setOpaque(false);
        search.add(label("Tracking Number"));
        JTextField q = new JTextField(20);
        search.add(q);
        JButton b = red("SEARCH");
        search.add(b);
        JPanel[] panels = {box("PARCEL DETAILS"), box("TRACKING HISTORY")};
        JPanel res = new JPanel(new GridLayout(1, 2, 10, 0));
        res.setOpaque(false);
        res.add(panels[0]);
        res.add(panels[1]);
        Runnable run = () -> {
            String key = q.getText().trim();
            Parcel p = find(key);
            panels[0].removeAll();
            panels[1].removeAll();
            if (key.isEmpty()) {
                panels[0].add(lbl("Enter a tracking number and press SEARCH.", 15, Color.DARK_GRAY));
            } else if (p == null) {
                panels[0].add(lbl("No parcel found.", 15, Color.DARK_GRAY));
            } else {
                JPanel a = panels[0];
                a.setLayout(new GridLayout(0, 2, 5, 5));
                detail(a, "Tracking Number", p.tr);
                detail(a, "Sender", p.sender);
                detail(a, "Receiver", p.receiver);
                detail(a, "Destination", p.dest);
                detail(a, "COD", p.cod > 0 ? codText(p) : "None");
                detail(a, "Status", p.status);
                detail(a, "Rider", p.rider);
                StringBuilder sb = new StringBuilder();
                try {
                    for (String[] h : DB.loadHistory(p.tr))
                            sb.append("● ").append(h[1]).append("\n ").append(h[3]).append("\n ").append(h[2]).append("\n\n");
                } catch (SQLException ex) {
                    sb.append("Could not load history: ").append(ex.getMessage());
                }
                if (sb.length() == 0) sb.append("No tracking history yet.");
                JTextArea h = new JTextArea(sb.toString());
                h.setEditable(false);
                h.setFont(new Font("Arial", Font.PLAIN, 13));
                panels[1].add(h);
            }
            panels[0].revalidate();
            panels[1].revalidate();
            panels[0].repaint();
            panels[1].repaint();
        };
        b.addActionListener(e -> run.run());
        q.addActionListener(e -> run.run());
        run.run();
        root.add(search, BorderLayout.NORTH);
        root.add(res, BorderLayout.CENTER);
        setPage("TRACK PARCEL", root);
    }
    void detail(JPanel p, String a, String b) {
        p.add(label(a));
        p.add(lbl(": " + b, 12, Color.DARK_GRAY));
    }
    Parcel find(String q) {
        for (Parcel p : parcels)
                if (p.tr.equalsIgnoreCase(q))
                return p;
        return null;
    }
    void history() {
        shell("TRACKING HISTORY");
        JPanel root = pageRoot(8);
        String[] c = {"Tracking Number", "Date & Time", "Location", "Status", "Remarks"};
        ArrayList<String[]> hist = new ArrayList<>();
        try {
            hist = DB.loadHistory(null);
        } catch (SQLException ex) {
            dbError(ex);
        }
        Object[][] d = new Object[hist.size()][5];
        for (int i = 0; i < hist.size(); i++)
                d[i] = hist.get(i);
        JTable t = readOnlyTable(d, c);
        JPanel bar = filterBar(t, "Status",
                new String[]{"All", "Pending", "Parcel Picked Up", "Arrived at Sorting Facility", "On the Way", "Delivered"}, 3, 16);
        root.add(bar, BorderLayout.NORTH);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        setPage("TRACKING HISTORY", root);
    }
    void refreshRiders() {
        try {
            ArrayList<Rider> f = DB.loadRiders();
            riders.clear();
            riders.addAll(f);
        } catch (SQLException ex) {
            dbError(ex);
        }
    }
    Rider freshRider(Rider r) {
        refreshRiders();
        for (Rider x : riders)
                if (x.id == r.id)
                return x;
        return r;
    }
    void riders() {
        shell("MONITOR RIDERS");
        refreshRiders();
        JPanel root = pageRoot(8);
        String[] c = {"Rider", "Area", "Parcels", "Capacity", "Status", "Action"};
        Object[][] d = new Object[riders.size()][6];
        for (int i = 0; i < riders.size(); i++) {
            Rider r = riders.get(i);
            d[i] = new Object[]{r.name, r.area, r.total, r.total + " / " + MAX_PARCELS, r.status, "MANAGE"};
        }
        JTable t = new JTable(new DefaultTableModel(d, c) {
            public boolean isCellEditable(int r, int col) {
                return col == 5;
            }
        });
        style(t);
        t.setRowHeight(38);
        t.getColumnModel().getColumn(5).setCellRenderer(new BtnRenderer());
        t.getColumnModel().getColumn(5).setCellEditor(new RiderEditor(new JCheckBox(), t));
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() >= 0)
                        riderReport(riders.get(t.convertRowIndexToModel(t.getSelectedRow())));
            }
        });
        JPanel bar = filterBar(t, "Status", new String[]{"All", "Active", "Inactive"}, 4, 16);
        JButton add = red("+ ADD RIDER");
        add.addActionListener(e -> addRider());
        JPanel top = rightBar();
        top.add(add);
        root.add(northBar(bar, top), BorderLayout.NORTH);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        root.add(lbl("Click MANAGE for View Report / Update / Archive. Double-click a rider to open the rider report.",
                12, Color.DARK_GRAY), BorderLayout.SOUTH);
        setPage("MONITOR RIDERS", root);
    }
    void addRider() {
        JTextField n = new JTextField();
        JTextField a = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 2, 7, 7));
        p.add(new JLabel("Rider Name"));
        p.add(n);
        p.add(new JLabel("Assigned Area"));
        p.add(a);
        if (form(p, "Add Rider")) {
            if (n.getText().trim().isEmpty() || a.getText().trim().isEmpty()) {
                warn("Please fill in all fields.");
                return;
            }
            try {
                riders.add(DB.insertRider(n.getText().trim(), a.getText().trim()));
            } catch (SQLException ex) {
                dbError(ex);
            }
            riders();
        }
    }
    void updateRider(Rider rd) {
        JTextField n = new JTextField(rd.name);
        JTextField a = new JTextField(rd.area);
        JComboBox<String> st = new JComboBox<>(new String[]{"Active", "Inactive"});
        st.setSelectedItem(rd.status);
        JPanel p = new JPanel(new GridLayout(0, 2, 7, 7));
        p.add(new JLabel("Rider Name"));
        p.add(n);
        p.add(new JLabel("Assigned Area"));
        p.add(a);
        p.add(new JLabel("Status"));
        p.add(st);
        if (form(p, "Update Rider")) {
            if (n.getText().trim().isEmpty() || a.getText().trim().isEmpty()) {
                warn("Please fill in all fields.");
                return;
            }
            String old = rd.name;
            String nn = n.getText().trim();
            try {
                DB.updateRider(rd.id, nn, a.getText().trim(), (String) st.getSelectedItem());
                rd.name = nn;
                rd.area = a.getText().trim();
                rd.status = (String) st.getSelectedItem();
                for (Parcel pc : parcels)
                        if (pc.rider != null && pc.rider.equals(old))
                        pc.rider = nn;
            } catch (SQLException ex) {
                dbError(ex);
            }
        }
    }
    void riderActions(int r) {
        Rider rd = riders.get(r);
        boolean active = rd.status.equals("Active");
        Object[] o = {"VIEW REPORT", "UPDATE", active ? "ARCHIVE" : "RESTORE", "CANCEL"};
        int z = JOptionPane.showOptionDialog(this, "Choose action for " + rd.name, "Rider Action", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, o, o[0]);
        if (z == 0) {
            riderReport(rd);
            return;
        }
        if (z == 1)
                updateRider(rd);
        else if (z == 2) {
            String msg = active
                    ? "Archive " + rd.name + "? The rider becomes Inactive (nothing is deleted)."
                    : "Restore " + rd.name + " to Active?";
            if (confirm(msg, "Confirm")) {
                try {
                    String ns = active ? "Inactive" : "Active";
                    DB.setRiderStatus(rd.id, ns);
                    rd.status = ns;
                } catch (SQLException ex) {
                    dbError(ex);
                }
            }
        }
        riders();
    }
    void riderReport(Rider rr) {
        Rider r = freshRider(rr);
        JPanel root = pageRoot(10);
        JPanel info = box("RIDER REPORT — " + r.name);
        info.setLayout(new GridLayout(0, 4, 10, 6));
        detail(info, "Rider", r.name);
        detail(info, "Assigned Area", r.area);
        detail(info, "Status", r.status);
        detail(info, "Total Parcels", r.total + " / " + MAX_PARCELS);
        detail(info, "Delivered", String.valueOf(r.delivered));
        detail(info, "COD Parcels", String.valueOf(r.cod));
        detail(info, "Paid Parcels", String.valueOf(r.paid));
        detail(info, "Unpaid COD", String.valueOf(Math.max(0, r.cod - r.paid)));
        detail(info, "COD Amount", peso(r.codAmt));
        detail(info, "Collected", peso(r.colAmt));
        detail(info, "Remitted", peso(r.colPaid));
        detail(info, "Remittance Due", peso(r.remittanceDue()));
        ArrayList<Parcel> mine = new ArrayList<>();
        int dl = 0, pd = 0, ow = 0;
        for (Parcel p : parcels) {
            if (!p.archived && p.rider != null && p.rider.equals(r.name)) {
                mine.add(p);
                if (p.status.equals("Delivered")) dl++;
                else if (p.status.equals("Pending")) pd++;
                else if (p.status.equals("On the Way")) ow++;
            }
        }
        String[] c = {"Tracking Number", "Receiver", "Destination", "Status", "Date"};
        Object[][] d = new Object[mine.size()][5];
        for (int i = 0; i < mine.size(); i++) {
            Parcel p = mine.get(i);
            d[i] = new Object[]{p.tr, p.receiver, p.dest, p.status, p.date};
        }
        JTable pt = readOnlyTable(d, c);
        JPanel cat = box("PARCELS BY CATEGORY — Delivered: " + dl + " Pending: " + pd + " On the Way: " + ow);
        cat.add(filterBar(pt, "Category", STATUS_ALL, 3, 14), BorderLayout.NORTH);
        cat.add(new JScrollPane(pt), BorderLayout.CENTER);
        JPanel g = box("DELIVERY PERFORMANCE");
        g.add(new RiderChart(r));
        JButton back = red("BACK TO RIDERS");
        back.addActionListener(e -> riders());
        JPanel bottom = rightBar();
        bottom.add(back);
        root.add(info, BorderLayout.NORTH);
        root.add(cat, BorderLayout.CENTER);
        root.add(g, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        setPage("RIDER REPORT", root);
    }
    void reports() {
        shell("ADMIN REPORTS");
        if (currentUser == null || !currentUser.canViewReports()) {
            setPage("REPORTS", new JLabel("Reports are available to Admin accounts only.", SwingConstants.CENTER));
            return;
        }
        JPanel root = pageRoot(8);
        refreshRiders();
        JTabbedPane tabs = new JTabbedPane();
        String[] c = {"Rider", "Parcels", "Delivered", "COD", "Paid", "Unpaid COD", "Status"};
        Object[][] d = new Object[riders.size()][7];
        for (int i = 0; i < riders.size(); i++) {
            Rider r = riders.get(i);
            d[i] = new Object[]{r.name, r.total, r.delivered, r.cod, r.paid,
                        Math.max(0, r.cod - r.paid), r.status};
        }
        JTable t = readOnlyTable(d, c);
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() >= 0)
                        riderReport(riders.get(t.convertRowIndexToModel(t.getSelectedRow())));
            }
        });
        JPanel rp = new JPanel(new BorderLayout());
        rp.add(filterBar(t, "Status", new String[]{"All", "Active", "Inactive"}, 6, 16), BorderLayout.NORTH);
        rp.add(new JScrollPane(t), BorderLayout.CENTER);
        rp.add(new DeliveryChart(), BorderLayout.EAST);
        JTable pt = table(false);
        JPanel ps = new JPanel(new BorderLayout());
        ps.add(filterBar(pt, "Status", STATUS_ALL, 2, 16), BorderLayout.NORTH);
        ps.add(new JScrollPane(pt), BorderLayout.CENTER);
        String[] pc = {"Rider", "COD Parcels", "Paid Parcels", "Unpaid COD Parcels", "COD Amount", "Paid Amount"};
        Object[][] pdata = new Object[riders.size()][6];
        for (int i = 0; i < riders.size(); i++) {
            Rider r = riders.get(i);
            pdata[i] = new Object[]{r.name, r.cod, r.paid, Math.max(0, r.cod - r.paid),
                        peso(r.codAmt), peso(r.paidAmt)};
        }
        JTable payT = readOnlyTable(pdata, pc);
        JPanel pay = new JPanel(new BorderLayout());
        pay.add(filterBar(payT, null, null, 0, 16), BorderLayout.NORTH);
        pay.add(new JScrollPane(payT), BorderLayout.CENTER);
        String[] rc = {"Rider", "COD Amount", "Collected (Delivered)", "Remitted", "Remittance Due", "Pending COD (Not Delivered)"};
        Object[][] rdata = new Object[riders.size()][6];
        double due = 0;
        for (int i = 0; i < riders.size(); i++) {
            Rider r = riders.get(i);
            rdata[i] = new Object[]{r.name, peso(r.codAmt), peso(r.colAmt), peso(r.colPaid),
                        peso(r.remittanceDue()), peso(r.pendingCod())};
            due += r.remittanceDue();
        }
        JTable remT = readOnlyTable(rdata, rc);
        JPanel rem = new JPanel(new BorderLayout());
        rem.add(filterBar(remT, null, null, 0, 16), BorderLayout.NORTH);
        rem.add(new JScrollPane(remT), BorderLayout.CENTER);
        rem.add(lbl(" Total remittance due: " + peso(due)
                + " (Remittance Due = COD collected on delivered parcels − amount already remitted)",
                13, Color.DARK_GRAY), BorderLayout.SOUTH);
        tabs.addTab("Rider Report", rp);
        tabs.addTab("Parcel Summary", ps);
        tabs.addTab("Payment Report", pay);
        tabs.addTab("Remittance", rem);
        JButton refresh = red("REFRESH");
        refresh.addActionListener(e -> {
            loadData();
            reports();
        });
        JPanel top = rightBar();
        top.add(refresh);
        root.add(top, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setPage("REPORTS", root);
    }
    void addTicket() {
        ArrayList<Parcel> act = new ArrayList<>();
        for (Parcel pc : parcels)
                if (!pc.archived)
                act.add(pc);
        if (act.isEmpty()) {
            warn("Add a parcel first. A ticket must belong to a parcel.");
            return;
        }
        JComboBox<String> pc = new JComboBox<>();
        for (Parcel x : act)
                pc.addItem(x.tr);
        JLabel rc = new JLabel(act.get(0).receiver);
        pc.addActionListener(e -> {
            Parcel sel = find((String) pc.getSelectedItem());
            if (sel != null)
                    rc.setText(sel.receiver);
        });
        JTextField cn = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 2, 7, 7));
        p.add(new JLabel("Tracking Number"));
        p.add(pc);
        p.add(new JLabel("Receiver"));
        p.add(rc);
        p.add(new JLabel("Concern"));
        p.add(cn);
        if (form(p, "Add Ticket")) {
            if (cn.getText().trim().isEmpty()) {
                warn("Please type the concern.");
                return;
            }
            try {
                String id = DB.nextTicketId();
                String tr = (String) pc.getSelectedItem();
                DB.insertTicket(id, tr, cn.getText().trim());
                tickets.add(new Ticket(id, tr, find(tr).receiver, cn.getText().trim(), "Open", "-", false));
            } catch (SQLException ex) {
                dbError(ex);
            }
            viewTicketArchive = false;
            tickets();
        }
    }
    void tickets() {
        shell("TICKETS / CONCERNS");
        JPanel root = pageRoot(8);
        ticketRows = new ArrayList<>();
        for (Ticket tk : tickets)
                if (tk.archived == viewTicketArchive)
                ticketRows.add(tk);
        String[] c = {"", "Ticket ID", "Parcel", "Receiver", "Concern", "Status", "Closed By"};
        Object[][] d = new Object[ticketRows.size()][7];
        for (int i = 0; i < ticketRows.size(); i++) {
            Ticket tk = ticketRows.get(i);
            d[i] = new Object[]{false, tk.id, tk.tr, tk.receiver, tk.concern, tk.status, tk.closedBy};
        }
        JTable t = new JTable(new DefaultTableModel(d, c) {
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            public boolean isCellEditable(int r, int col) {
                return col == 0;
            }
        });
        style(t);
        t.getColumnModel().getColumn(0).setMaxWidth(45);
        JPanel bar = filterBar(t, "Status", new String[]{"All", "Open", "Closed"}, 5, 16);
        JComboBox<String> view = new JComboBox<>(new String[]{"Active Tickets", "Archived Tickets"});
        view.setSelectedIndex(viewTicketArchive ? 1 : 0);
        view.addActionListener(e -> {
            viewTicketArchive = view.getSelectedIndex() == 1;
            tickets();
        });
        JButton add = red("+ ADD TICKET");
        add.addActionListener(e -> addTicket());
        JButton close = red("CLOSE SELECTED");
        close.addActionListener(e -> {
            int n = 0;
            TableModel m = t.getModel();
            for (int i = 0; i < m.getRowCount(); i++) {
                if (Boolean.TRUE.equals(m.getValueAt(i, 0))) {
                    Ticket tk = ticketRows.get(i);
                    if (tk.status.equals("Open")) {
                        try {
                            DB.closeTicket(tk.id, currentUser.getUsername());
                            tk.status = "Closed";
                            tk.closedBy = loggedUser;
                            n++;
                        } catch (SQLException ex) {
                            dbError(ex);
                        }
                    }
                }
            }
            if (n == 0)
                    warn("Select an OPEN ticket.");
            else
                    tickets();
        });
        JButton arch = red(viewTicketArchive ? "RESTORE SELECTED" : "ARCHIVE SELECTED");
        arch.addActionListener(e -> {
            int n = 0;
            TableModel m = t.getModel();
            for (int i = 0; i < m.getRowCount(); i++) {
                if (Boolean.TRUE.equals(m.getValueAt(i, 0))) {
                    Ticket tk = ticketRows.get(i);
                    try {
                        DB.setTicketArchived(tk.id, !viewTicketArchive);
                        tk.archived = !viewTicketArchive;
                        n++;
                    } catch (SQLException ex) {
                        dbError(ex);
                    }
                }
            }
            if (n == 0)
                    warn("Select at least one ticket.");
            else
                    tickets();
        });
        JPanel top = rightBar();
        top.add(view);
        if (!viewTicketArchive) {
            top.add(add);
            top.add(close);
        }
        top.add(arch);
        root.add(northBar(bar, top), BorderLayout.NORTH);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        setPage("TICKETS / CONCERNS", root);
    }
    class Truck extends JPanel {
        Truck() {
            setPreferredSize(new Dimension(520, 210));
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D x = (Graphics2D) g;
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int y = 70;
            x.setColor(RED);
            x.fillRoundRect(70, y, 270, 100, 8, 8);
            x.setColor(Color.WHITE);
            x.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
            x.drawString("J&T", 110, y + 58);
            x.setFont(new Font("Arial", Font.BOLD, 14));
            x.drawString("EXPRESS", 178, y + 57);
            Polygon cab = new Polygon(
                    new int[]{340, 405, 455, 455, 340},
                    new int[]{y + 20, y + 20, y + 60, y + 100, y + 100}, 5);
            x.setColor(RED);
            x.fillPolygon(cab);
            x.setColor(new Color(45, 55, 60));
            x.fillRect(360, y + 30, 45, 28);
            x.fillRect(410, y + 30, 30, 28);
            x.setColor(Color.DARK_GRAY);
            x.fillOval(110, y + 75, 50, 50);
            x.fillOval(380, y + 75, 50, 50);
            x.setColor(Color.LIGHT_GRAY);
            x.fillOval(124, y + 89, 22, 22);
            x.fillOval(394, y + 89, 22, 22);
            x.setColor(Color.GRAY);
            x.drawLine(45, 190, 470, 190);
        }
    }
    class Chart extends JPanel {
        int[] v;
        Chart(int pending, int transit, int delivered) {
            v = new int[]{pending, transit, delivered};
            setPreferredSize(new Dimension(300, 230));
            setBackground(Color.WHITE);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] n = {"Pending", "On the Way", "Delivered"};
            int base = getHeight() - 45;
            int max = Math.max(1, Math.max(v[0], Math.max(v[1], v[2])));
            int maxH = Math.max(50, base - 40);
            for (int i = 0; i < 3; i++) {
                int x = 45 + i * 78;
                int h = v[i] * maxH / max;
                g.setColor(i == 0 ? new Color(235, 160, 30)
                        : i == 1 ? new Color(40, 135, 205)
                        : new Color(45, 175, 80));
                g.fillRect(x, base - h, 45, h);
                g.setColor(Color.DARK_GRAY);
                g.drawString("" + v[i], x + 15, base - h - 6);
                g.drawString(n[i], x - 2, base + 20);
            }
        }
    }
    class DeliveryChart extends JPanel {
        DeliveryChart() {
            setPreferredSize(new Dimension(300, 260));
            setBackground(Color.WHITE);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ArrayList<Rider> act = new ArrayList<>();
            for (Rider r : riders)
                    if (r.status.equals("Active"))
                    act.add(r);
            int base = getHeight() - 45;
            int step = Math.max(28, (getWidth() - 30) / Math.max(1, act.size()));
            int bw = Math.max(14, step - 14);
            int mx = 1;
            for (Rider q : act)
                    mx = Math.max(mx, q.delivered);
            for (int i = 0; i < act.size(); i++) {
                Rider r = act.get(i);
                int x = 15 + i * step;
                int h = r.delivered * 170 / mx;
                g.setColor(new Color(45, 135, 205));
                g.fillRect(x, base - h, bw, h);
                g.setColor(Color.DARK_GRAY);
                g.drawString("" + r.delivered, x + 2, base - h - 5);
                g.drawString(r.name.split(" ")[0], x, base + 18);
            }
            g.drawString("Delivered Parcels per Rider", 55, 20);
        }
    }
    class RiderChart extends JPanel {
        Rider r;
        RiderChart(Rider r) {
            this.r = r;
            setPreferredSize(new Dimension(310, 300));
            setBackground(Color.WHITE);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int[] v = {r.total, r.delivered, r.cod, r.paid};
            String[] n = {"Total", "Delivered", "COD", "Paid"};
            int base = getHeight() - 45;
            int mx = Math.max(1, Math.max(Math.max(v[0], v[1]), Math.max(v[2], v[3])));
            for (int i = 0; i < 4; i++) {
                int x = 18 + i * 72;
                int h = v[i] * 210 / mx;
                g.setColor(i == 0 ? Color.GRAY
                        : i == 1 ? new Color(45, 175, 80)
                        : i == 2 ? new Color(235, 160, 30)
                        : new Color(45, 135, 205));
                g.fillRect(x, base - h, 45, h);
                g.setColor(Color.DARK_GRAY);
                g.drawString("" + v[i], x + 12, base - h - 5);
                g.drawString(n[i], x + 3, base + 20);
            }
        }
    }
    class BtnRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        BtnRenderer() {
            setBackground(Color.WHITE);
            setForeground(RED);
            setFont(new Font("Arial", Font.BOLD, 11));
        }
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean s, boolean f, int r, int c) {
            setText("" + v);
            return this;
        }
    }
    class ParcelEditor extends DefaultCellEditor {
        JButton b = new JButton("EDIT / ARCHIVE");
        JTable t;
        ParcelEditor(JCheckBox c, JTable t) {
            super(c);
            this.t = t;
            b.addActionListener(e -> {
                int vr = t.getSelectedRow();
                fireEditingStopped();
                if (vr < 0)
                        return;
                Parcel p = parcelRows.get(t.convertRowIndexToModel(vr));
                if (!viewParcelArchive) {
                    Object[] o = {"EDIT", "ARCHIVE", "CANCEL"};
                    int z = JOptionPane.showOptionDialog(Main.this,
                            "Choose action for " + p.tr, "Parcel Action",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, o, o[0]);
                    if (z == 0)
                            editParcel(p);
                    else if (z == 1) {
                        if (confirm("Archive " + p.tr + "? It moves to Archived Parcels (nothing is deleted).",
                                "Archive Parcel"))
                                archiveParcel(p, true);
                    }
                } else {
                    if (confirm("Restore " + p.tr + " to active parcels?", "Restore Parcel"))
                            archiveParcel(p, false);
                }
                parcelManagement();
            });
        }
        public Object getCellEditorValue() {
            return b.getText();
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            b.setText("" + v);
            return b;
        }
    }
    class RiderEditor extends DefaultCellEditor {
        JButton b = new JButton("MANAGE");
        JTable t;
        RiderEditor(JCheckBox c, JTable t) {
            super(c);
            this.t = t;
            b.addActionListener(e -> {
                int vr = t.getSelectedRow();
                fireEditingStopped();
                if (vr >= 0)
                        riderActions(t.convertRowIndexToModel(vr));
            });
        }
        public Object getCellEditorValue() {
            return b.getText();
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            b.setText("" + v);
            return b;
        }
    }
    static class Rider {
        int id;
        String name, area, status;
        int total, delivered, cod, paid;
        double codAmt, paidAmt, colAmt, colPaid;
        Rider(int id, String n, String a, int t, int d, int c, int p, double ca, double pa,
                double col, double colp, String st) {
            this.id = id; name = n; area = a; total = t; delivered = d;
            cod = c; paid = p; codAmt = ca; paidAmt = pa; colAmt = col; colPaid = colp; status = st;
        }
        double remittanceDue() {
            return Math.max(0, colAmt - colPaid);
        }
        double pendingCod() {
            return Math.max(0, codAmt - colAmt);
        }
    }
    static class Parcel {
        String tr, sender, receiver, dest, status, date, rider;
        boolean archived, paid;
        double cod;
        Parcel(String t, String s, String r, String d, String st, String dt, String ri,
                boolean ar, double cod, boolean paid) {
            tr = t; sender = s; receiver = r; dest = d;
            status = st; date = dt; rider = ri; archived = ar;
            this.cod = cod; this.paid = paid;
        }
    }
    static class Ticket {
        String id, tr, receiver, concern, status, closedBy;
        boolean archived;
        Ticket(String i, String tr, String r, String c, String s, String cb, boolean ar) {
            id = i; this.tr = tr; receiver = r; concern = c; status = s; closedBy = cb; archived = ar;
        }
    }
    public static void main(String[] a) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
