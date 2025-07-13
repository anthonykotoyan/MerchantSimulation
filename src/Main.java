import java.util.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static boolean running = true;
    public static int numTicks = 0;
    private static SimulationGUI gui;
    public static final List<String> eventLog = new ArrayList<>();

    public static void main(String[] args) {
        Item.createItemPool(20, 5);
        Merchant.createMerchants(3);
        initInventory();
        ValuationChart.setTypePrices();

        SwingUtilities.invokeLater(() -> {
            gui = new SimulationGUI(new Main());
            gui.setVisible(true);
        });
    }

    public void tick() {
        System.out.println("\nTick: " + numTicks + "\n");
        Merchant.tickAll();
        OrderBook.tickAll();
        ValuationChart.setTypePrices();
        numTicks++;
    }

    public static void initInventory() {
        for (int i = 0; i < Item.ITEM_POOL.length; i++) {
            int merchantIndex = (int) (Math.random() * Merchant.MERCHANTS.length);
            Merchant.MERCHANTS[merchantIndex].inventory.add(Item.ITEM_POOL[i]);
        }
    }

    public static void printToStringMerchants() {
        for (Merchant merchant : Merchant.MERCHANTS) {
            System.out.println(merchant.toString());
            System.out.println();
        }
    }

}

