import java.util.UUID;

public class Order {

    public enum Type { BUY, SELL }
    public final String id;
    public final Type type;
    public final Merchant owner;
    public final Item item;
    public final double price;
    public double age;
    public boolean executed = false;

    public Order(Type type, Merchant owner, Item item, double price) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.owner = owner;
        this.item = item;
        this.price = price;
        this.age = 0.0;
        this.item.order = this;
    }

    public void execute(Merchant other) {
        String event = "";
        if (type == Type.BUY) {
            if (owner.wealth >= price && other.inventory.contains(item)) {
                owner.wealth -= price;
                other.wealth += price;
                other.inventory.remove(item);
                owner.inventory.add(item);

                event = String.format("EXEC_BUY: %s bought %s from %s for %.2f", owner.name, item.name, other.name, price);
            } else {
                event = String.format("FAIL_BUY: %s failed to buy %s from %s for %.2f", owner.name, item.name, other.name, price);
            }
        } else if (type == Type.SELL) {
            if (owner.inventory.contains(item) && other.wealth >= price) {
                owner.wealth += price;
                other.wealth -= price;
                owner.inventory.remove(item);
                other.inventory.add(item);
                event = String.format("EXEC_SELL: %s sold %s to %s for %.2f", owner.name, item.name, other.name, price);
            } else {
                event = String.format("FAIL_SELL: %s failed to sell %s to %s for %.2f", owner.name, item.name, other.name, price);
            }
        }
        Main.eventLog.add(String.format("[%d] %s", Main.numTicks, event));
        this.executed = true;
        OrderBook.removeOrder(this);
    }

    public void tick() {
        age += 1.0;
    }

    public String toString() {
        return String.format("ID: %s... | %s | %s | %.2f",
                id.substring(0, 8), type, item.name, price);
    }

    public String getDetailedString() {
        return String.format(
            "Order Details:\n" +
            "  ID: %s\n" +
            "  Type: %s\n" +
            "  Owner: %s\n" +
            "  Item: %s\n" +
            "  Condition: %.2f\n" +
            "  Price: %.2f\n" +
            "  Age: %.0f ticks",
            id, type, (owner != null ? owner.name : "N/A"), item.name, item.condition, price, age
        );
    }
}
