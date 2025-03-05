import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import burp.api.montoya.persistence.Preferences;

public class CustomHeadersConfig {

    private JPanel mainPanel;
    private JTable headersTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton saveButton;
    private JCheckBox enableHeadersCheckbox;
    private final int MAX_HEADERS = 10;
    private final Preferences preferences;

    public CustomHeadersConfig(Preferences preferences) {
        this.preferences = preferences;

        // Initialize UI components
        initializeUI();

        // Load saved headers from preferences
        loadSavedHeaders();
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Create top panel with checkbox
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enableHeadersCheckbox = new JCheckBox("Enable Custom Headers", true);

        // Load enabled state from preferences
        Boolean enabled = preferences.getBoolean("enable_headers");
        if (enabled != null) {
            enableHeadersCheckbox.setSelected(enabled);
        }

        // Add listener to save state changes
        enableHeadersCheckbox.addActionListener(e -> {
            preferences.setBoolean("enable_headers", enableHeadersCheckbox.isSelected());
        });

        topPanel.add(enableHeadersCheckbox);

        // Create table for headers
        String[] columnNames = {"Header Name", "Header Value", "Enabled"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 2 ? Boolean.class : String.class;
            }
        };

        headersTable = new JTable(tableModel);
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        headersTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        headersTable.getColumnModel().getColumn(2).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(headersTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Header");
        removeButton = new JButton("Remove Header");
        saveButton = new JButton("Save Configuration");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);

        // Add action listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() < MAX_HEADERS) {
                    tableModel.addRow(new Object[]{"", "", true});
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Maximum of " + MAX_HEADERS + " headers allowed.",
                            "Limit Reached",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = headersTable.getSelectedRow();
                if (selectedRow != -1) {
                    tableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Please select a header to remove.",
                            "No Selection",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveHeaders();
                JOptionPane.showMessageDialog(mainPanel,
                        "Headers configuration saved successfully!",
                        "Save Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Assemble the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set border and size
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void saveHeaders() {
        // First, clear any existing saved headers
        clearSavedHeaders();

        // Save the current set of headers
        int headerCount = tableModel.getRowCount();
        preferences.setInteger("header_count", headerCount);

        for (int i = 0; i < headerCount; i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            boolean enabled = (Boolean) tableModel.getValueAt(i, 2);

            preferences.setString("header_name_" + i, name);
            preferences.setString("header_value_" + i, value);
            preferences.setBoolean("header_enabled_" + i, enabled);
        }
    }

    private void loadSavedHeaders() {
        // Clear the current table
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // Load headers from preferences
        Integer headerCount = preferences.getInteger("header_count");
        if (headerCount == null || headerCount == 0) {
            // Add a default header if no saved headers exist
            addDefaultHeader("X-Custom-Header", "CustomValue");
            return;
        }

        for (int i = 0; i < headerCount; i++) {
            String name = preferences.getString("header_name_" + i);
            String value = preferences.getString("header_value_" + i);
            Boolean enabled = preferences.getBoolean("header_enabled_" + i);

            if (name != null && value != null && enabled != null) {
                tableModel.addRow(new Object[]{name, value, enabled});
            }
        }
    }

    private void clearSavedHeaders() {
        Integer headerCount = preferences.getInteger("header_count");
        if (headerCount == null) {
            return;
        }

        for (int i = 0; i < headerCount; i++) {
            preferences.deleteString("header_name_" + i);
            preferences.deleteString("header_value_" + i);
            preferences.deleteBoolean("header_enabled_" + i);
        }
    }

    private void addDefaultHeader(String name, String value) {
        tableModel.addRow(new Object[]{name, value, true});
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public boolean isEnabled() {
        return enableHeadersCheckbox.isSelected();
    }

    public List<CustomHeader> getHeaders() {
        List<CustomHeader> headers = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            boolean enabled = (Boolean) tableModel.getValueAt(i, 2);

            if (enabled && name != null && !name.trim().isEmpty()) {
                headers.add(new CustomHeader(name, value, enabled));
            }
        }

        return headers;
    }

    // Inner class to represent a custom header
    public static class CustomHeader {
        private String name;
        private String value;
        private boolean enabled;

        public CustomHeader(String name, String value, boolean enabled) {
            this.name = name;
            this.value = value;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}