import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import burp.api.montoya.persistence.Preferences;

/**
 * Custom headers configuration panel and data manager.
 * Provides UI for adding, editing, and configuring custom HTTP headers
 * with support for static and dynamic values.
 */
public class CustomHeadersConfig {

    // Constants
    private static final int MAX_HEADERS = 10;
    private static final String DEFAULT_REGEX = "Authorization:\\s*Bearer\\s+([A-Za-z0-9._-]+)";

    // UI Components
    private JPanel mainPanel;
    private JTable headersTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton saveButton;
    private JCheckBox enableHeadersCheckbox;

    // Data storage
    private final Preferences preferences;
    private final Map<String, Color> colorMap = new HashMap<>();
    private final Map<Integer, Color> rowColors = new HashMap<>();

    /**
     * Constructs a new CustomHeadersConfig with the given preferences.
     *
     * @param preferences The preferences used to store configuration
     */
    public CustomHeadersConfig(Preferences preferences) {
        this.preferences = preferences;

        // Initialize color map
        initializeColorMap();

        // Initialize UI components
        initializeUI();

        // Load saved headers from preferences
        loadSavedHeaders();
    }

    /**
     * Initializes the color map with predefined colors.
     */
    private void initializeColorMap() {
        colorMap.put("None", null);
        colorMap.put("Red", new Color(205, 3, 3));
        colorMap.put("Green", new Color(15, 189, 15));
        colorMap.put("Blue", new Color(16, 80, 200));
        colorMap.put("Yellow", new Color(179, 130, 7));
        colorMap.put("Orange", new Color(255, 152, 48));
        colorMap.put("Purple", new Color(112, 24, 230));
        colorMap.put("Pink", new Color(193, 17, 102));
        colorMap.put("Teal", new Color(5, 184, 184));
    }

    /**
     * Updates the help text in the pattern dialog based on the selected match type.
     *
     * @param helpText The help text component to update
     * @param isRegex  Whether regex mode is selected
     */
    private void updateHelpText(JTextArea helpText, boolean isRegex) {
        if (isRegex) {
            helpText.setText("Regex pattern to extract value from the macro's responses. Use capturing groups () " +
                    "for precise extraction. Example: Authorization:\\s*Bearer\\s+([\\w.-]+)");
        } else {
            helpText.setText("Simple string to search for in the macro's responses. The value immediately " +
                    "following this string will be used. Example: \"access_token\":\"");
        }
    }

    /**
     * Initializes all UI components.
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Create top panel with checkbox
        JPanel topPanel = createTopPanel();

        // Create table for headers
        JScrollPane tableScrollPane = createHeadersTable();

        // Create the button panel
        JPanel buttonPanel = createButtonPanel();

        // Assemble the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set border and size
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Creates and returns the top panel with enable checkbox.
     *
     * @return The configured top panel
     */
    private JPanel createTopPanel() {
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
        return topPanel;
    }

    /**
     * Creates the headers table and returns it in a scroll pane.
     *
     * @return ScrollPane containing the headers table
     */
    private JScrollPane createHeadersTable() {
        // Create table model with column definitions
        String[] columnNames = {"Header Name", "Header Value", "Enabled", "Dynamic"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 2 || column == 3 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Make the Value column non-editable if Dynamic is checked
                if (column == 1) {
                    Boolean isDynamic = (Boolean) getValueAt(row, 3);
                    return !isDynamic;
                }
                return true;
            }
        };

        // Create and configure the table
        headersTable = new JTable(tableModel);
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        headersTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        headersTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        headersTable.getColumnModel().getColumn(3).setPreferredWidth(60);

        // Configure custom renderers for normal and boolean cells
        configureTableRenderers();

        // Add listener for Dynamic checkbox
        configureTableModelListener();

        // Create and configure popup menu
        JPopupMenu popupMenu = createPopupMenu();
        addPopupMenuMouseListener(popupMenu);

        // Create and return a scroll pane containing the table
        JScrollPane scrollPane = new JScrollPane(headersTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        return scrollPane;
    }

    /**
     * Configures custom cell renderers for the table.
     */
    private void configureTableRenderers() {
        // Create a custom cell renderer to show row colors for normal cells
        TableCellRenderer defaultRenderer = headersTable.getDefaultRenderer(Object.class);
        headersTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // Gray out the value cell if Dynamic is checked
                if (column == 1 && row < tableModel.getRowCount()) {
                    Boolean isDynamic = (Boolean) tableModel.getValueAt(row, 3);
                    if (isDynamic) {
                        c.setForeground(Color.GRAY);
                        if (c instanceof JComponent) {
                            ((JComponent) c).setToolTipText("Value will be extracted dynamically");
                        }
                    } else {
                        c.setForeground(table.getForeground());
                    }
                }

                // Apply row colors
                applyRowColor(c, table, isSelected, row);
                return c;
            }
        });

        // Custom renderer for boolean cells (checkbox columns)
        TableCellRenderer booleanRenderer = headersTable.getDefaultRenderer(Boolean.class);
        headersTable.setDefaultRenderer(Boolean.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = booleanRenderer.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // Apply row colors
                applyRowColor(c, table, isSelected, row);

                // Add tooltip for Dynamic checkbox
                if (column == 3 && c instanceof JComponent) {
                    ((JComponent) c).setToolTipText("Extract value from macro response");
                }

                return c;
            }
        });
    }

    /**
     * Applies row color to a component if defined.
     *
     * @param c The component to apply color to
     * @param table The table containing the component
     * @param isSelected Whether the component is selected
     * @param row The row index
     */
    private void applyRowColor(Component c, JTable table, boolean isSelected, int row) {
        if (!isSelected && rowColors.containsKey(row)) {
            c.setBackground(rowColors.get(row));
        } else if (isSelected) {
            c.setBackground(table.getSelectionBackground());
        } else {
            c.setBackground(table.getBackground());
        }
    }

    /**
     * Configures the table model listener for dynamic checkbox interaction.
     */
    private void configureTableModelListener() {
        headersTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3) {  // Dynamic column
                int row = e.getFirstRow();
                boolean isDynamic = (Boolean) tableModel.getValueAt(row, 3);

                if (isDynamic) {
                    // If dynamic is checked, store the current value as a placeholder
                    String currentValue = (String) tableModel.getValueAt(row, 1);
                    preferences.setString("header_dynamic_placeholder_" + row, currentValue);

                    // Set to a placeholder value
                    tableModel.setValueAt("Dynamic", row, 1);
                } else {
                    // If dynamic is unchecked, restore the placeholder value if available
                    String placeholder = preferences.getString("header_dynamic_placeholder_" + row);
                    if (placeholder != null) {
                        tableModel.setValueAt(placeholder, row, 1);
                    }
                }

                // Repaint to show UI changes
                headersTable.repaint();
            }
        });
    }

    /**
     * Creates and configures the popup menu for right-click actions.
     *
     * @return The configured popup menu
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Add color menu
        JMenu colorMenu = new JMenu("Set Row Color");
        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            JMenuItem colorItem = createColorMenuItem(entry.getKey(), entry.getValue());
            colorMenu.add(colorItem);
        }
        popupMenu.add(colorMenu);

        // Add extraction pattern menu item
        JMenuItem patternItem = new JMenuItem("Set Extraction Pattern...");
        patternItem.addActionListener(e -> showExtractionPatternDialog());
        popupMenu.add(patternItem);

        return popupMenu;
    }

    /**
     * Creates a color menu item.
     *
     * @param colorName The name of the color
     * @param color The color value
     * @return The configured menu item
     */
    private JMenuItem createColorMenuItem(String colorName, Color color) {
        JMenuItem colorItem = new JMenuItem(colorName);
        // Set background color for the menu item itself to provide visual cue
        if (color != null) {
            colorItem.setBackground(color);
        }

        colorItem.addActionListener(e -> {
            int selectedRow = headersTable.getSelectedRow();
            if (selectedRow != -1) {
                if ("None".equals(colorName)) {
                    rowColors.remove(selectedRow);
                } else {
                    rowColors.put(selectedRow, color);
                }
                // Save row color to preferences
                preferences.setString("header_color_" + selectedRow, colorName);
                // Repaint the table to show the new color
                headersTable.repaint();
            }
        });

        return colorItem;
    }

    /**
     * Adds a mouse listener to show the popup menu on right-click.
     *
     * @param popupMenu The popup menu to show
     */
    private void addPopupMenuMouseListener(JPopupMenu popupMenu) {
        headersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopupTrigger(e, popupMenu);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopupTrigger(e, popupMenu);
            }
        });
    }

    /**
     * Handles popup trigger events.
     *
     * @param e The mouse event
     * @param popupMenu The popup menu to show
     */
    private void handlePopupTrigger(MouseEvent e, JPopupMenu popupMenu) {
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            int row = headersTable.rowAtPoint(e.getPoint());
            if (row >= 0 && row < headersTable.getRowCount()) {
                headersTable.setRowSelectionInterval(row, row);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * Shows the extraction pattern configuration dialog.
     */
    private void showExtractionPatternDialog() {
        int selectedRow = headersTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        Boolean isDynamic = (Boolean) tableModel.getValueAt(selectedRow, 3);
        if (!isDynamic) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Please check the 'Dynamic' option first to enable extraction.",
                    "Dynamic Extraction",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Get current configuration
        String currentPattern = preferences.getString("header_regex_" + selectedRow);
        boolean isRegex = preferences.getBoolean("header_isregex_" + selectedRow) != null ?
                preferences.getBoolean("header_isregex_" + selectedRow) : true;

        // Create dialog for pattern settings
        JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));

        // Add radio buttons for match type
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();
        JRadioButton regexButton = new JRadioButton("Regex Pattern", isRegex);
        JRadioButton stringButton = new JRadioButton("Simple String", !isRegex);
        group.add(regexButton);
        group.add(stringButton);
        radioPanel.add(regexButton);
        radioPanel.add(stringButton);

        // Add text field for pattern
        JTextField patternField = new JTextField(30);
        patternField.setText(currentPattern != null ? currentPattern :
                (isRegex ? DEFAULT_REGEX : ""));

        // Add help text
        JTextArea helpText = new JTextArea(4, 30);
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setBackground(new Color(240, 240, 240));
        updateHelpText(helpText, regexButton.isSelected());

        // Add listener to update help text
        regexButton.addActionListener(radioEvent -> updateHelpText(helpText, true));
        stringButton.addActionListener(radioEvent -> updateHelpText(helpText, false));

        // Assemble dialog
        dialogPanel.add(radioPanel, BorderLayout.NORTH);
        dialogPanel.add(patternField, BorderLayout.CENTER);
        dialogPanel.add(helpText, BorderLayout.SOUTH);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                dialogPanel,
                "Extraction Pattern",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pattern = patternField.getText();
            if (pattern != null && !pattern.trim().isEmpty()) {
                // Save both the pattern and whether it's a regex
                preferences.setString("header_regex_" + selectedRow, pattern);
                preferences.setBoolean("header_isregex_" + selectedRow, regexButton.isSelected());
            }
        }
    }

    /**
     * Creates and configures the button panel.
     *
     * @return The configured button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Header");
        removeButton = new JButton("Remove Header");
        saveButton = new JButton("Save Configuration");

        // Add action listeners
        addButton.addActionListener(e -> addHeader());
        removeButton.addActionListener(e -> removeHeader());
        saveButton.addActionListener(e -> saveHeaders());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);

        // Add help button
        JButton helpButton = new JButton("?");
        helpButton.setMargin(new Insets(0, 4, 0, 4));
        helpButton.addActionListener(e -> showHelpDialog());
        buttonPanel.add(helpButton);

        return buttonPanel;
    }

    /**
     * Shows the help dialog.
     */
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(mainPanel,
                "CustomHeaderZ Configuration Help\n\n" +
                        "Header Configuration:\n" +
                        "- Header Name: Name of the HTTP header to add (Static)\n" +
                        "- Enabled: Check to include this header in requests\n" +
                        "- Dynamic: Check to extract value from macro responses (Optional)\n\n" +
                        "Additional Features:\n" +
                        "- Row Colors: Right-click any row to set its color\n" +
                        "- Extraction Pattern: Right-click a dynamic row to set regex pattern\n\n" +
                        "To extract values from the macros previous responses in Burp, you must:\n" +
                        "1. Check the 'Dynamic' option for the header\n" +
                        "2. Set an extraction pattern via right-click menu\n" +
                        "3. Configure a Burp session handling rule with a macro\n\n" +
                        "Pattern Extraction:\n" +
                        "- Regex Pattern: Use capturing groups () to extract specific values\n" +
                        "- Simple String: Extracts text after the search string up to next delimiter\n\n",
                "Configuration Help",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Adds a new header to the table.
     */
    private void addHeader() {
        if (tableModel.getRowCount() < MAX_HEADERS) {
            tableModel.addRow(new Object[]{"", "", true, false});
        } else {
            JOptionPane.showMessageDialog(mainPanel,
                    "Maximum of " + MAX_HEADERS + " headers allowed.",
                    "Limit Reached",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Removes the selected header from the table.
     */
    private void removeHeader() {
        int selectedRow = headersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Please select a header to remove.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Remove the color information for the row
        rowColors.remove(selectedRow);

        // Remove dynamic extraction pattern for the row
        preferences.deleteString("header_regex_" + selectedRow);
        preferences.deleteString("header_dynamic_placeholder_" + selectedRow);

        // Remove the row from the table
        tableModel.removeRow(selectedRow);

        // Update rowColors map for remaining rows
        Map<Integer, Color> updatedRowColors = new HashMap<>();
        for (Map.Entry<Integer, Color> entry : rowColors.entrySet()) {
            int row = entry.getKey();
            if (row > selectedRow) {
                updatedRowColors.put(row - 1, entry.getValue());
            } else if (row < selectedRow) {
                updatedRowColors.put(row, entry.getValue());
            }
        }
        rowColors.clear();
        rowColors.putAll(updatedRowColors);
    }

    /**
     * Saves the current headers configuration to preferences.
     */
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
            boolean dynamic = (Boolean) tableModel.getValueAt(i, 3);

            preferences.setString("header_name_" + i, name);
            preferences.setString("header_value_" + i, value);
            preferences.setBoolean("header_enabled_" + i, enabled);
            preferences.setBoolean("header_dynamic_" + i, dynamic);

            // Save row color
            if (rowColors.containsKey(i)) {
                // Find color name from the color
                for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().equals(rowColors.get(i))) {
                        preferences.setString("header_color_" + i, entry.getKey());
                        break;
                    }
                }
            } else {
                preferences.setString("header_color_" + i, "None");
            }
        }

        JOptionPane.showMessageDialog(mainPanel,
                "Headers configuration saved successfully!",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Loads saved headers from preferences.
     */
    private void loadSavedHeaders() {
        // Clear the current table
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // Clear the row colors map
        rowColors.clear();

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
            Boolean dynamic = preferences.getBoolean("header_dynamic_" + i);
            String colorName = preferences.getString("header_color_" + i);

            if (name != null && value != null && enabled != null) {
                // If dynamic was null (for backward compatibility), default to false
                if (dynamic == null) dynamic = false;

                tableModel.addRow(new Object[]{name, value, enabled, dynamic});

                // Load row color if available
                if (colorName != null && colorMap.containsKey(colorName) && !colorName.equals("None")) {
                    rowColors.put(i, colorMap.get(colorName));
                }
            }
        }
    }

    /**
     * Clears saved headers from preferences.
     */
    private void clearSavedHeaders() {
        Integer headerCount = preferences.getInteger("header_count");
        if (headerCount == null) {
            return;
        }

        for (int i = 0; i < headerCount; i++) {
            preferences.deleteString("header_name_" + i);
            preferences.deleteString("header_value_" + i);
            preferences.deleteBoolean("header_enabled_" + i);
            preferences.deleteBoolean("header_dynamic_" + i);
            preferences.deleteString("header_color_" + i);
            // We don't delete regex patterns here to allow for reuse if headers are readded
        }
    }

    /**
     * Adds a default header to the table.
     *
     * @param name The header name
     * @param value The header value
     */
    private void addDefaultHeader(String name, String value) {
        tableModel.addRow(new Object[]{name, value, true, false});
    }

    /**
     * Gets the main panel for UI display.
     *
     * @return The main configuration panel
     */
    public JPanel getPanel() {
        return mainPanel;
    }

    /**
     * Checks if custom headers are enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enableHeadersCheckbox.isSelected();
    }

    /**
     * Gets the extraction pattern for a specific header row.
     *
     * @param row The row index of the header
     * @return The pattern string, or the default pattern if none is set
     */
    public String getPattern(int row) {
        String pattern = preferences.getString("header_regex_" + row);
        return pattern != null ? pattern : DEFAULT_REGEX;
    }

    /**
     * Checks if a row uses regex extraction (as opposed to simple string matching).
     *
     * @param row The row index of the header
     * @return True if regex extraction is used, false for simple string matching
     */
    public boolean isRegexExtraction(int row) {
        Boolean isRegex = preferences.getBoolean("header_isregex_" + row);
        // Default to regex for backward compatibility
        return isRegex == null || isRegex;
    }

    /**
     * Gets the list of all configured headers.
     *
     * @return List of CustomHeader objects
     */
    public List<CustomHeader> getHeaders() {
        List<CustomHeader> headers = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            boolean enabled = (Boolean) tableModel.getValueAt(i, 2);
            boolean dynamic = (Boolean) tableModel.getValueAt(i, 3);
            String colorName = "None";

            // Find color name for this row if it has a color
            if (rowColors.containsKey(i)) {
                for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().equals(rowColors.get(i))) {
                        colorName = entry.getKey();
                        break;
                    }
                }
            }

            if (enabled && name != null && !name.trim().isEmpty()) {
                String pattern = dynamic ? getPattern(i) : "";
                boolean isRegex = isRegexExtraction(i);
                headers.add(new CustomHeader(name, value, enabled, dynamic, colorName, pattern, isRegex));
            }
        }

        return headers;
    }

    /**
     * Inner class to represent a custom header.
     */
    public static class CustomHeader {
        private final String name;
        private final String value;
        private final boolean enabled;
        private final boolean dynamic;
        private final String colorName;
        private final String pattern;
        private final boolean isRegex;

        /**
         * Creates a basic custom header without dynamic features.
         *
         * @param name    The header name
         * @param value   The header value
         * @param enabled Whether the header is enabled
         */
        public CustomHeader(String name, String value, boolean enabled) {
            this(name, value, enabled, false, "None", "", true);
        }

        /**
         * Creates a custom header with all options.
         *
         * @param name      The header name
         * @param value     The header value
         * @param enabled   Whether the header is enabled
         * @param dynamic   Whether the header value is dynamically extracted
         * @param colorName The color name for UI display
         * @param pattern   The extraction pattern for dynamic headers
         * @param isRegex   Whether the pattern is a regex or simple string
         */
        public CustomHeader(String name, String value, boolean enabled, boolean dynamic,
                            String colorName, String pattern, boolean isRegex) {
            this.name = name;
            this.value = value;
            this.enabled = enabled;
            this.dynamic = dynamic;
            this.colorName = colorName;
            this.pattern = pattern;
            this.isRegex = isRegex;
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

        public boolean isDynamic() {
            return dynamic;
        }

        public String getColorName() {
            return colorName;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isRegex() {
            return isRegex;
        }
    }
}