import java.util.ArrayList;

public class OrderBook {
    public static ArrayList<Order> ORDERS = new ArrayList<>();
    public static ArrayList<Order> ORDER_HISTORY = new ArrayList<>();

    public static void addOrder(Order order) {
        ORDERS.add(order);
        ORDER_HISTORY.add(order);
    }

    public static void removeOrder(Order order) {
        ORDERS.remove(order);
    }

    public static void tickAll() {
        for (Order order : ORDERS) {
            order.tick();
        }
        clearExecutedOrders();
    }

    public static void clearExecutedOrders() {
        ORDERS.removeIf(order -> order.executed || order.removed);
    }
}
