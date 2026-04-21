import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GymTrackerUI extends JFrame {

    // ── Change these to match your MySQL setup ─────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/gym_tracker";
    static final String DB_USER = "root";
    static final String DB_PASS = "Krishna*369";  
    // ───────────────────────────────────────────────────────────

    // Colors
    static final Color BG      = new Color(245, 245, 243);
    static final Color WHITE   = Color.WHITE;
    static final Color DARK    = new Color(25,  25,  25);
    static final Color MUTED   = new Color(120, 120, 115);
    static final Color BORDER  = new Color(220, 220, 218);
    static final Color STAT_BG = new Color(235, 235, 232);
    static final Color ROW_ALT = new Color(250, 250, 248);
    static final Color RED     = new Color(200,  60,  50);

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblTotal, lblWeek, lblBest, lblUniq;

    public GymTrackerUI() {
        setTitle("Gym Progress Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 640);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        setVisible(true);
        loadWorkouts();
    }

    // ══════════════════════════════════════════════════════════
    //  TOP BAR
    // ══════════════════════════════════════════════════════════
    JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(WHITE);

        JLabel title = new JLabel("Gym Progress Tracker");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(DARK);

        JLabel sub = new JLabel("Log and track your training sessions");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        JButton addBtn = makeButton("+ Add Workout", DARK, WHITE);
        addBtn.addActionListener(e -> showDialog(-1, "", "push", "", "3", "10", "0"));

        bar.add(left,   BorderLayout.WEST);
        bar.add(addBtn, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════
    //  MAIN CONTENT
    // ══════════════════════════════════════════════════════════
    JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.add(buildStatCards(), BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        return content;
    }

    // ══════════════════════════════════════════════════════════
    //  STAT CARDS
    // ══════════════════════════════════════════════════════════
    JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setBackground(BG);
        row.setPreferredSize(new Dimension(0, 80));

        lblTotal = addStatCard(row, "Total Sessions", "0");
        lblWeek  = addStatCard(row, "This Week",      "0");
        lblBest  = addStatCard(row, "Best Lift (kg)", "0");
        lblUniq  = addStatCard(row, "Exercises",      "0");
        return row;
    }

    JLabel addStatCard(JPanel parent, String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(STAT_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 24));
        val.setForeground(DARK);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        parent.add(card);
        return val;
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE CARD
    // ══════════════════════════════════════════════════════════
    JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);

        JLabel h = new JLabel("Workout Log");
        h.setFont(new Font("SansSerif", Font.BOLD, 14));
        h.setForeground(DARK);
        header.add(h, BorderLayout.WEST);

        JButton refresh = makeButton("↻ Refresh", STAT_BG, DARK);
        refresh.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(5, 12, 5, 12)));
        refresh.addActionListener(e -> loadWorkouts());
        header.add(refresh, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Table model
        String[] cols = {"ID", "Exercise", "Date", "Sets", "Reps", "Weight (kg)", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(new Color(240, 240, 238));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // Table header style
        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 11));
        th.setForeground(MUTED);
        th.setBackground(WHITE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        th.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) th.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);

        // Cell renderer (alternating rows)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) setBackground(r % 2 == 0 ? WHITE : ROW_ALT);
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                setForeground(DARK);
                return this;
            }
        });

        // Action buttons in last column
        table.getColumnModel().getColumn(6).setCellRenderer(
            (t, v, sel, foc, r, c) -> makeActionPanel(r % 2 == 0 ? WHITE : ROW_ALT)
        );

        // Mouse click handler for Edit / Delete
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != 6) return;

                int    id   = (int)    tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                String date = (String) tableModel.getValueAt(row, 2);
                String sets = String.valueOf(tableModel.getValueAt(row, 3));
                String reps = String.valueOf(tableModel.getValueAt(row, 4));
                String wt   = String.valueOf(tableModel.getValueAt(row, 5));

                Rectangle cell = table.getCellRect(row, col, false);
                int relX = e.getX() - cell.x;

                if (relX < cell.width / 2) {
                    showDialog(id, name, "push", date, sets, reps, wt);
                } else {
                    int ok = JOptionPane.showConfirmDialog(GymTrackerUI.this,
                        "Delete \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (ok == JOptionPane.YES_OPTION) deleteWorkout(id);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    JPanel makeActionPanel(Color bg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        p.setBackground(bg);

        JButton edit = makeButton("Edit", STAT_BG, DARK);
        edit.setFont(new Font("SansSerif", Font.PLAIN, 11));
        edit.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(3, 10, 3, 10)));

        JButton del = makeButton("Delete", new Color(255, 240, 240), RED);
        del.setFont(new Font("SansSerif", Font.PLAIN, 11));
        del.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 200, 200), 1, true), new EmptyBorder(3, 10, 3, 10)));

        p.add(edit);
        p.add(del);
        return p;
    }

    // ══════════════════════════════════════════════════════════
    //  ADD / EDIT DIALOG
    // ══════════════════════════════════════════════════════════
    void showDialog(int id, String name, String cat, String date,
                    String sets, String reps, String weight) {

        boolean isEdit = id > 0;
        JDialog dlg = new JDialog(this, isEdit ? "Edit Workout" : "Add Workout", true);
        dlg.setSize(400, 430);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 0, 4, 0);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField fName   = styledField(name);
        JTextField fDate   = styledField(date.isEmpty() ?
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) : date);
        JTextField fSets   = styledField(sets);
        JTextField fReps   = styledField(reps);
        JTextField fWeight = styledField(weight);
        JComboBox<String> fCat = new JComboBox<>(new String[]{"push","pull","legs","cardio"});
        fCat.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fCat.setSelectedItem(cat);

        addRow(form, gc, 0, "Exercise Name",     fName);
        addRow(form, gc, 1, "Category",          fCat);
        addRow(form, gc, 2, "Date (YYYY-MM-DD)", fDate);
        addRow(form, gc, 3, "Sets",              fSets);
        addRow(form, gc, 4, "Reps",              fReps);
        addRow(form, gc, 5, "Weight (kg)",       fWeight);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnRow.setBackground(WHITE);
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton cancel = makeButton("Cancel", STAT_BG, DARK);
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = makeButton(isEdit ? "Update" : "Save Workout", DARK, WHITE);
        save.addActionListener(e -> {
            String n = fName.getText().trim();
            String d = fDate.getText().trim();
            if (n.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and date are required.");
                return;
            }
            try {
                int    s  = Integer.parseInt(fSets.getText().trim());
                int    r  = Integer.parseInt(fReps.getText().trim());
                double w  = Double.parseDouble(fWeight.getText().trim());
                String ct = (String) fCat.getSelectedItem();
                if (isEdit) updateWorkout(id, n, d, s, r, w);
                else        addWorkout(n, ct, d, s, r, w);
                dlg.dispose();
                loadWorkouts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Sets, Reps, and Weight must be numbers.");
            }
        });

        btnRow.add(cancel);
        btnRow.add(save);
        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    void addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(MUTED);
        form.add(lbl, gc);
        gc.gridy = row * 2 + 1;
        form.add(field, gc);
    }

    // ══════════════════════════════════════════════════════════
    //  DATABASE OPERATIONS
    // ══════════════════════════════════════════════════════════
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    void loadWorkouts() {
        tableModel.setRowCount(0);
        int total = 0, week = 0, uniq = 0;
        double best = 0;
        java.util.Set<String> names = new java.util.HashSet<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM workouts ORDER BY date DESC");
             ResultSet rs = ps.executeQuery()) {

            java.util.Date now = new java.util.Date();
            while (rs.next()) {
                int    id   = rs.getInt("id");
                String name = rs.getString("exercise_name");
                String date = rs.getString("date");
                int    sets = rs.getInt("sets");
                int    reps = rs.getInt("reps");
                double wt   = rs.getDouble("weight");

                tableModel.addRow(new Object[]{id, name, date, sets, reps, wt, ""});
                total++;
                names.add(name);
                if (wt > best) best = wt;

                try {
                    java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd");
                    long diff = (now.getTime() - sdf.parse(date).getTime())
                                / (1000L * 60 * 60 * 24);
                    if (diff <= 7) week++;
                } catch (Exception ignored) {}
            }
            uniq = names.size();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to database.\n\n" + e.getMessage()
                + "\n\nMake sure:\n1. MySQL is running\n2. DB credentials are correct\n3. JAR is added",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }

        lblTotal.setText(String.valueOf(total));
        lblWeek.setText(String.valueOf(week));
        lblBest.setText(String.valueOf(best));
        lblUniq.setText(String.valueOf(uniq));
    }

    void addWorkout(String name, String cat, String date, int sets, int reps, double weight) {
        String sql = "INSERT INTO workouts (exercise_name, date, sets, reps, weight) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, date);
            ps.setInt(3, sets);
            ps.setInt(4, reps);
            ps.setDouble(5, weight);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding: " + e.getMessage());
        }
    }

    void updateWorkout(int id, String name, String date, int sets, int reps, double weight) {
        String sql = "UPDATE workouts SET exercise_name=?, date=?, sets=?, reps=?, weight=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, date);
            ps.setInt(3, sets);
            ps.setInt(4, reps);
            ps.setDouble(5, weight);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating: " + e.getMessage());
        }
    }

    void deleteWorkout(int id) {
        String sql = "DELETE FROM workouts WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadWorkouts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════
    JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        return f;
    }

    // ══════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(GymTrackerUI::new);
    }
}