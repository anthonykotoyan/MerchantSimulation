import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SimulationGUI extends JFrame {

    private final Main mainApp;
    private JList<String> merchantList;
    private JList<Order> orderList;
    private JList<String> priceList; // New component for item prices
    private JTextArea eventLogArea;
    private JPanel detailsPanel;
    private CardLayout detailsCardLayout;
    private JTextArea detailsTextArea;
    private PriceHistoryPanel priceHistoryPanel;
    private DefaultListModel<String> merchantListModel;
    private DefaultListModel<Order> orderListModel;
    private DefaultListModel<String> priceListModel; // New model for prices

    public SimulationGUI(Main mainApp) {
        this.mainApp = mainApp;
        initUI();
    }

    private void initUI() {
        setTitle("Trading Simulation");
        setSize(1400, 800); // Increased width to accommodate price panel
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton tickButton = new JButton("Tick");

        // Add text field for number of ticks
        JTextField tickCountField = new JTextField("1", 3);
        tickCountField.setToolTipText("Number of ticks to run");

        tickButton.addActionListener(e -> {
            try {
                int tickCount = Integer.parseInt(tickCountField.getText().trim());
                if (tickCount > 0) {
                    for (int i = 0; i < tickCount; i++) {
                        mainApp.tick();
                    }
                    updateGUI();
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a positive number", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid integer", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(new JLabel("Ticks:"));
        controlPanel.add(tickCountField);
        controlPanel.add(tickButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Merchant List
        merchantListModel = new DefaultListModel<>();
        merchantList = new JList<>(merchantListModel);
        merchantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        merchantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                orderList.clearSelection();
                updateDetailsArea();
            }
        });
        JScrollPane merchantListScrollPane = new JScrollPane(merchantList);
        merchantListScrollPane.setBorder(BorderFactory.createTitledBorder("Merchants"));

        // Order Book List
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                merchantList.clearSelection();
                updateDetailsArea();
            }
        });
        JScrollPane orderListScrollPane = new JScrollPane(orderList);
        orderListScrollPane.setBorder(BorderFactory.createTitledBorder("Order Book"));

        // Item Prices List
        priceListModel = new DefaultListModel<>();
        priceList = new JList<>(priceListModel);
        priceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        priceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                merchantList.clearSelection();
                orderList.clearSelection();
                updateDetailsArea();
            }
        });
        JScrollPane priceListScrollPane = new JScrollPane(priceList);
        priceListScrollPane.setBorder(BorderFactory.createTitledBorder("Item Prices"));

        // Details Panel with CardLayout for text and graph
        detailsCardLayout = new CardLayout();
        detailsPanel = new JPanel(detailsCardLayout);
        // Text card
        detailsTextArea = new JTextArea();
        detailsTextArea.setEditable(false);
        detailsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane textScrollPane = new JScrollPane(detailsTextArea);
        textScrollPane.setBorder(BorderFactory.createTitledBorder("Details"));
        detailsPanel.add(textScrollPane, "TEXT");
        // Graph card using custom PriceHistoryPanel
        priceHistoryPanel = new PriceHistoryPanel(Collections.emptyList());
        priceHistoryPanel.setBorder(BorderFactory.createTitledBorder("Price History"));
        detailsPanel.add(priceHistoryPanel, "CHART");

        // Event Log
        eventLogArea = new JTextArea();
        eventLogArea.setEditable(false);
        eventLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane eventLogScrollPane = new JScrollPane(eventLogArea);
        eventLogScrollPane.setBorder(BorderFactory.createTitledBorder("Event Log"));

        // Left Panel with three sections
        JSplitPane leftTopSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, merchantListScrollPane, orderListScrollPane);
        leftTopSplit.setResizeWeight(0.5);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTopSplit, priceListScrollPane);
        leftSplit.setResizeWeight(0.7);

        // Center Panel
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftSplit, detailsPanel);
        centerSplit.setResizeWeight(0.6);

        // Main Split Pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerSplit, eventLogScrollPane);
        mainSplitPane.setResizeWeight(0.6);

        mainPanel.add(mainSplitPane, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        updateMerchantList();
        updateGUI();
    }

    private void updateGUI() {
        updateMerchantList();
        updateOrderList();
        updatePriceList();
        updateEventLog();
        updateDetailsArea();

        setTitle("Trading Simulation - Tick: "  + ((Main.numTicks - 1 == -1) ? "Initial State" : (Main.numTicks - 1)));
    }

    private void updateMerchantList() {
        int selectedIndex = merchantList.getSelectedIndex();
        String selectedMerchantName = null;
        if (selectedIndex != -1 && selectedIndex < merchantListModel.size()) {
            selectedMerchantName = merchantListModel.getElementAt(selectedIndex);
        }

        merchantListModel.clear();

        // Sort merchants by wealth (highest to lowest)
        java.util.Arrays.sort(Merchant.MERCHANTS, (m1, m2) -> Double.compare(m2.wealth, m1.wealth));

        for (Merchant merchant : Merchant.MERCHANTS) {
            String displayText = String.format("%s {%.2f}", merchant.name, merchant.wealth);
            merchantListModel.addElement(displayText);
        }

        // Restore selection if possible
        if (selectedMerchantName != null) {
            for (int i = 0; i < merchantListModel.size(); i++) {
                if (merchantListModel.getElementAt(i).startsWith(selectedMerchantName.split(" \\{")[0])) {
                    merchantList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void updateOrderList() {
        int selectedIndex = orderList.getSelectedIndex();
        orderListModel.clear();
        for (Order order : OrderBook.ORDER_HISTORY) {
            orderListModel.addElement(order);
        }
        if (selectedIndex != -1 && selectedIndex < orderListModel.size()) {
            orderList.setSelectedIndex(selectedIndex);
        }
    }

    private void updatePriceList() {
        priceListModel.clear();
        if (Item.ITEM_NAMES != null && ValuationChart.TYPE_PRICES != null) {
            for (int i = 0; i < Item.ITEM_NAMES.length && i < ValuationChart.TYPE_PRICES.length; i++) {
                if (Item.ITEM_NAMES[i] != null) {
                    String priceDisplay = String.format("%s: $%.2f (%s)", Item.ITEM_NAMES[i], ValuationChart.TYPE_PRICES[i], Item.ITEM_TYPE_COUNTS[i]);
                    priceListModel.addElement(priceDisplay);
                }
            }
        }
        // Force GUI refresh
        priceList.revalidate();
        priceList.repaint();
    }

    private void updateEventLog() {
        StringBuilder sb = new StringBuilder();
        for (String event : Main.eventLog) {
            sb.append(event).append("\n");
        }
        eventLogArea.setText(sb.toString());
        // Scroll to bottom
        eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength());
    }

    private void updateDetailsArea() {
        // Price history selected: show graph
        if (priceList.getSelectedIndex() != -1) {
            int idx = priceList.getSelectedIndex();
            List<Double> history = ValuationChart.TYPE_PRICE_HISTORY.get(idx);
            priceHistoryPanel.setHistory(history);
            priceHistoryPanel.repaint();
            detailsCardLayout.show(detailsPanel, "CHART");
            return;
        }
        // Merchant selected: show text
        if (merchantList.getSelectedIndex() != -1) {
            Merchant selectedMerchant = Merchant.MERCHANTS[merchantList.getSelectedIndex()];
            detailsTextArea.setText(selectedMerchant.getStats());
            detailsCardLayout.show(detailsPanel, "TEXT");
        } else if (orderList.getSelectedIndex() != -1) {
            Order selectedOrder = orderList.getSelectedValue();
            detailsTextArea.setText(selectedOrder.getDetailedString());
            detailsCardLayout.show(detailsPanel, "TEXT");
        } else {
            detailsTextArea.setText("Select a merchant, order, or price item to see details.");
            detailsCardLayout.show(detailsPanel, "TEXT");
        }
    }
}

