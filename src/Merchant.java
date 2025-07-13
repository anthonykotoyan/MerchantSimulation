import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Merchant {
    public static Merchant[] MERCHANTS = new Merchant[0];

    public static final double CONDITION_WEIGHT = 3.0;

    public String name;
    public double wealth = 200.0;
    public ArrayList<Item> inventory = new ArrayList<>();
    public ArrayList<Order> orders = new ArrayList<Order>();

    // attributes for the merchant:

    // desire attributes
    public double risk = Math.random();
    public double ambition = Math.random();
    public double composure = Math.random();
    public double materialism = Math.random();

    // sell attributes
    public double greed = Math.random();

    // buy attributes
    public double lavishness = Math.random();

    // attributes for the merchant's behavior
    public double aggressiveness = (Math.random() * 5);
    public double patience = (Math.random() * 8)+4;

    public Merchant() {
        this.name = Util.nameGenerator(2, 3);
    }

    public static void createMerchants(int size, double initialWealth) {
        MERCHANTS = new Merchant[size];
        for (int i = 0; i < size; i++) {
            Merchant merchant = new Merchant();
            merchant.wealth = initialWealth;
            MERCHANTS[i] = merchant;
        }
    }

    public static void tickAll(){
        for (Merchant merchant : MERCHANTS) {
            merchant.tick();
        }
    }

    public void tick() {
        checkAllExpiredOrders();
        placePreferredBuyOrders();
        placePreferredSellOrders();
        execPreferredOrders();

    }
    public void checkExpiredOrders() {
        for (int i=0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.executed){
                orders.remove(i);
                i--;
            }
            else if (order.age >= patience) {
                orders.remove(i);
                i--;
                OrderBook.removeOrder(order);
                order.removed = true;

            }
            else if (order.type == Order.Type.BUY && wealth < order.price) {
                order.invalid = true;
                OrderBook.removeOrder(order);
                orders.remove(i);
                i--;
            }
            else if (order.type == Order.Type.SELL && !inventory.contains(order.item)) {
                order.invalid = true;
                OrderBook.removeOrder(order);
                orders.remove(i);
                i--;

            }
        }
    }

    public void checkAllExpiredOrders() {
        for (Merchant merchant : MERCHANTS) {
            merchant.checkExpiredOrders();
        }

    }

    public double calculateInventoryWorth() {
        double inventoryWorth = 0;
        for (Item item : inventory) {
            inventoryWorth += ValuationChart.getPrice(item.type) * (1 + item.condition);
        }
        return inventoryWorth;
    }

    public double[] calculateNetWealthAfterOrder(Order order, boolean exec) {
        double[] netWealth = new double[2];
        netWealth[0] = wealth;
        netWealth[1] = calculateInventoryWorth();

        int typeNeg = exec ? 1 : -1;

        if (order.type == Order.Type.BUY) {
            netWealth[0] -= typeNeg * order.price;
            netWealth[1] += typeNeg * ValuationChart.getPrice(order.item.type) * (1 + order.item.condition);
        } else if (order.type == Order.Type.SELL) {
            netWealth[0] += typeNeg * order.price;
            netWealth[1] -= typeNeg * ValuationChart.getPrice(order.item.type) * (1 + order.item.condition);
        }

        return netWealth;
    }

    public boolean itemAlreadyOrdered(Item item, Order.Type type) {
        for (Order order : orders) {
            if (type == order.type && order.item.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public double calculateDesire(Order order, boolean exec) {
        double[] netWealth = calculateNetWealthAfterOrder(order, exec);
        double changeInWealth = netWealth[0] - wealth;
        double changeInInventoryWorth = netWealth[1] - calculateInventoryWorth();
        double profit = changeInWealth + changeInInventoryWorth;
        return profit * ambition + Math.abs(profit) * risk + (changeInInventoryWorth) * materialism + (1 - materialism) * changeInWealth;
    }

    public double[][] desireToBuy() {
        double[] desires = new double[Item.ITEM_POOL.length];
        double[] prices = new double[Item.ITEM_POOL.length];

        for (int i = 0; i < Item.ITEM_POOL.length; i++) {
            Item item = Item.ITEM_POOL[i];
            if (inventory.contains(item)) {
                desires[i] = Double.NEGATIVE_INFINITY; // Don't buy items already in inventory
                continue;
            }
            if (itemAlreadyOrdered(item, Order.Type.BUY)) {
                desires[i] = Double.NEGATIVE_INFINITY; // Don't buy items already ordered
                continue;
            }


            double marketPrice = ValuationChart.getPrice(item.type);
            double valued_price = marketPrice * (1 + item.condition);
            double buy_price = valued_price * (0.5 + (lavishness) * 1.5);
            buy_price = Math.min(buy_price, wealth);

            Order simulatedOrder = new Order(Order.Type.BUY, null, item, buy_price);

            double desireScore = calculateDesire(simulatedOrder, false);

            double competitiveness = marketPrice / buy_price;
            competitiveness = Math.min(competitiveness, 2.0);
            double chanceToSell = (competitiveness - 0.5) / 1.5;
            chanceToSell = Math.max(0.0, Math.min(chanceToSell, 1.0));

            desires[i] = desireScore * chanceToSell * (1 + CONDITION_WEIGHT * item.condition);
            prices[i] = buy_price;
        }

        return new double[][]{desires, prices};
    }

    public double[][] desireToSell() {
        double[] desires = new double[inventory.size()];
        double[] prices = new double[inventory.size()];
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (itemAlreadyOrdered(item, Order.Type.SELL)) {
                desires[i] = Double.NEGATIVE_INFINITY; // Don't sell items already ordered
                continue;
            }
            double marketPrice = ValuationChart.getPrice(item.type);
            double valued_price = marketPrice * (1 + item.condition);
            double sell_price = valued_price * (0.5 + (greed) * 1.5);

            Order simulatedOrder = new Order(Order.Type.SELL, null, item, sell_price);

            double desireScore = calculateDesire(simulatedOrder, false);

            double competitiveness = marketPrice / sell_price;
            competitiveness = Math.min(competitiveness, 2.0);
            double chanceToSell = (competitiveness - 0.5) / 1.5;
            chanceToSell = Math.max(0.0, Math.min(chanceToSell, 1.0));

            desires[i] = desireScore * chanceToSell * (1 - CONDITION_WEIGHT * item.condition);
            prices[i] = sell_price;
        }

        return new double[][]{desires, prices};
    }

    public double[] desireToExec() {
        double[] desires = new double[OrderBook.ORDERS.size()];
        for (int i = 0; i < OrderBook.ORDERS.size(); i++) {
            if (OrderBook.ORDERS.get(i).owner == this) {
                desires[i] = Double.NEGATIVE_INFINITY; // Don't execute own orders
                continue;
            }
            if (OrderBook.ORDERS.get(i).type == Order.Type.BUY&&
                    !inventory.contains(OrderBook.ORDERS.get(i).item)) {
                desires[i] = Double.NEGATIVE_INFINITY; // Can't execute a buy order for an item not in inventory
                continue;
            }
            if (OrderBook.ORDERS.get(i).type == Order.Type.SELL&&
                    wealth < OrderBook.ORDERS.get(i).price) {
                desires[i] = Double.NEGATIVE_INFINITY; // Can't execute a sell order if can't afford it
                continue;
            }
            double desireScore = calculateDesire(OrderBook.ORDERS.get(i), true);
            if (OrderBook.ORDERS.get(i).type == Order.Type.BUY) {
                desireScore *= (1 + CONDITION_WEIGHT * OrderBook.ORDERS.get(i).item.condition);
            } else if (OrderBook.ORDERS.get(i).type == Order.Type.SELL) {
                desireScore *= (1 - CONDITION_WEIGHT * OrderBook.ORDERS.get(i).item.condition);
            }
            desires[i] = desireScore;
        }
        return desires;
    }

    public void placePreferredBuyOrders() {
        double[][] info = desireToBuy();
        double[] desires = info[0];
        double[] prices = info[1];
        int numToPlace = Math.min((int) (Math.random() * aggressiveness + 1), desires.length);

        for (int i = 0; i < numToPlace; i++) {
            double maxDesire = Double.NEGATIVE_INFINITY;
            int maxIndex = -1;

            for (int j = 0; j < desires.length; j++) {
                if (desires[j] > maxDesire) {
                    maxDesire = desires[j];
                    maxIndex = j;
                }
            }

            if (maxIndex != -1 && Math.random() * 2 > composure && desires[maxIndex] != 0) {
                Item item = Item.ITEM_POOL[maxIndex];

                if (prices[maxIndex] > wealth) {
                    desires[maxIndex] = Double.NEGATIVE_INFINITY; // Cannot afford, don't try again
                    continue;
                }

                Order order = new Order(Order.Type.BUY, this, item, prices[maxIndex]);
                orders.add(order);
                OrderBook.addOrder(order);
                Main.eventLog.add(String.format("[%d] PLACE: %s placed a BUY order for %s at %.2f", Main.numTicks, this.name, item.name, prices[maxIndex]));

                desires[maxIndex] = Double.NEGATIVE_INFINITY;
            }
        }
    }

    public void placePreferredSellOrders() {
        double[][] info = desireToSell();
        double[] desires = info[0];
        double[] prices = info[1];
        int numToPlace = Math.min((int) (Math.random() * aggressiveness + 1), desires.length);




        for (int i = 0; i < numToPlace; i++) {
            double maxDesire = Double.NEGATIVE_INFINITY;
            int maxIndex = -1;

            for (int j = 0; j < desires.length; j++) {
                if (desires[j] > maxDesire) {
                    maxDesire = desires[j];
                    maxIndex = j;
                }
            }

            if (maxIndex != -1 && Math.random() * 2 > composure) {

                Item item = inventory.get(maxIndex);
                Order order = new Order(Order.Type.SELL, this, item, prices[maxIndex]);
                orders.add(order);
                OrderBook.addOrder(order);
                Main.eventLog.add(String.format("[%d] PLACE: %s placed a SELL order for %s at %.2f", Main.numTicks, this.name, item.name, prices[maxIndex]));
                desires[maxIndex] = Double.NEGATIVE_INFINITY; // Mark as handled
            }
        }

    }

    public void execPreferredOrders() {
        double[] desires = desireToExec();
        // Snapshot current orders so modifications don't shift indices
        List<Order> availableOrders = new ArrayList<>(OrderBook.ORDERS);
        int numExecs = Math.min((int) (Math.random() * aggressiveness + 1), desires.length);
        for (int i = 0; i < numExecs; i++) {
            double maxDesire = Double.NEGATIVE_INFINITY;
            int maxIndex = -1;

            for (int j = 0; j < desires.length; j++) {
                if (desires[j] > maxDesire) {
                    maxDesire = desires[j];
                    maxIndex = j;
                }
            }
            if (maxIndex != -1 && Math.random() * 2 > composure) {
                // Ensure index is within our snapshot
                if (maxIndex < 0 || maxIndex >= availableOrders.size()) {
                    desires[maxIndex] = Double.NEGATIVE_INFINITY;
                    continue;
                }
                Order order = availableOrders.get(maxIndex);

                if (order.owner == this) {
                    desires[maxIndex] = Double.NEGATIVE_INFINITY; // Don't execute own orders
                    continue;
                }
                if (order.type == Order.Type.BUY && !inventory.contains(order.item)) {
                    desires[maxIndex] = Double.NEGATIVE_INFINITY; // Can't execute a buy order for an item not in inventory
                    continue;
                }
                if (order.type == Order.Type.SELL && wealth < order.price) {
                    desires[maxIndex] = Double.NEGATIVE_INFINITY; // Can't execute a sell order if can't afford it
                    continue;
                }
                order.execute(this);
                desires[maxIndex] = Double.NEGATIVE_INFINITY;
                ValuationChart.updateTypePrice(order.item.type, order.type, order.price, false);

            }
        }
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()); // Basic attributes
        sb.append("\n\nInventory:\n");

        if (inventory.isEmpty()) {
            sb.append("  [Empty]");
        } else {
            java.util.Map<String, java.util.List<Double>> itemConditions = new java.util.TreeMap<>();
            for (Item item : inventory) {
                itemConditions.putIfAbsent(item.name, new java.util.ArrayList<>());
                itemConditions.get(item.name).add(item.condition);
            }

            for (java.util.Map.Entry<String, java.util.List<Double>> entry : itemConditions.entrySet()) {
                String itemName = entry.getKey();
                java.util.List<Double> conditions = entry.getValue();
                sb.append(String.format("  - %s (%d): ", itemName, conditions.size()));
                java.util.List<String> roundedConditions = new java.util.ArrayList<>();
                for (double cond : conditions) {
                    roundedConditions.add(String.format("%.2f", cond));
                }
                sb.append(String.join(", ", roundedConditions));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String toString() {
        // add inventory worth to the string

        return String.format("Merchant: %s\nWealth: %.2f\nInventory Worth: %.2f\nRisk: %.2f\nAmbition: %.2f\nComposure: %.2f\nMaterialism: %.2f\nGreed: %.2f\nLavishness: %.2f\nAggressiveness: %.2f\nPatience: %.2f",
                name, wealth, calculateInventoryWorth(), risk, ambition, composure, materialism, greed, lavishness, aggressiveness, patience);
    }

    public void printStats(){
        System.out.println(toString());

        Map<String, List<Double>> itemConditions = new TreeMap<>();

        for (Item item : inventory) {
            itemConditions.putIfAbsent(item.name, new ArrayList<>());
            itemConditions.get(item.name).add(item.condition);
        }

        for (Map.Entry<String, List<Double>> entry : itemConditions.entrySet()) {
            String itemName = entry.getKey();
            List<Double> conditions = entry.getValue();

            System.out.print("    ");
            System.out.print(itemName + " - ");
            for (int i = 0; i < conditions.size(); i++) {
                System.out.print(Util.roundTo(conditions.get(i), 2));
                if (i != conditions.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        System.out.println();
    }

}
