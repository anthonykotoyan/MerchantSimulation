import java.util.ArrayList;

public class OrderBook {
    public static ArrayList<Order> ORDERS = new ArrayList<>();


    public static void addOrder(Order order) {
        ORDERS.add(order);
        ValuationChart.updateTypePrice(order.item.type, order.type, order.price, order.item.condition);
    }
    public static void removeOrder(Order order) {
        ORDERS.remove(order);
    }

    public static void  tickAll() {
        for (Order order : ORDERS) {
            order.tick();
        }
    }





}
