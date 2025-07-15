import java.util.UUID;

public class Order {

    public enum Type { BUY, SELL }
    public static int nextId = 1;
    public final int id;
    public final Type type;
    public final Merchant owner;
    public final Item item;
    public final double price;
    public double age;
    public boolean executed = false;
    public boolean removed = false;
    public boolean invalid = false;
    public Merchant executedBy = null;

    public Order(Type type, Merchant owner, Item item, double price) {
        this.id = nextId++;
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

                event = String.format("EXEC_BUY: %s bought %s (%.2f) from %s for %.2f [ID:%d]", owner.name, item.name, item.condition, other.name, price, id);
            } else {
                // Detailed failure reason for better debugging
                boolean buyerHasFunds = owner.wealth >= price;
                boolean sellerHasItem = other.inventory.contains(item);
                String reason = "";
                if (!buyerHasFunds && !sellerHasItem) {
                    reason = String.format(" (buyer insufficient funds: %.2f < %.2f AND seller doesn't have the item)", owner.wealth, price);
                } else if (!buyerHasFunds) {
                    reason = String.format(" (buyer insufficient funds: %.2f < %.2f)", owner.wealth, price);
                } else if (!sellerHasItem) {
                    reason = " (seller doesn't have the item)";
                }
                event = String.format("\uD83D\uDFE5FAIL_BUY: %s failed to buy %s (%.2f) from %s for %.2f%s [ID:%d]", owner.name, item.name, item.condition, other.name, price, reason, id);
            }
        } else if (type == Type.SELL) {
            if (owner.inventory.contains(item) && other.wealth >= price) {
                owner.wealth += price;
                other.wealth -= price;
                owner.inventory.remove(item);
                other.inventory.add(item);
                event = String.format("EXEC_SELL: %s sold %s (%.2f) to %s for %.2f [ID:%d]", owner.name, item.name, item.condition, other.name, price, id);
            } else {
                // Detailed failure reason for better debugging
                boolean ownerHasItem = owner.inventory.contains(item);
                boolean buyerHasWealth = other.wealth >= price;

                String reason = "";
                if (!ownerHasItem && !buyerHasWealth) {
                    reason = String.format(" (seller doesn't have item AND buyer insufficient funds: %.2f < %.2f)", other.wealth, price);
                } else if (!ownerHasItem) {
                    reason = " (seller doesn't have the item)";
                } else if (!buyerHasWealth) {
                    reason = String.format(" (buyer insufficient funds: %.2f < %.2f)", other.wealth, price);
                }

                event = String.format("\uD83D\uDFE5FAIL_SELL: %s failed to sell %s (%.2f) to %s for %.2f%s [ID:%d]", owner.name, item.name, item.condition, other.name, price, reason, id);
            }
        }
        Main.eventLog.add(String.format("[%d] %s", Main.numTicks, event));
        this.executed = true;
        this.executedBy = other;
        OrderBook.removeOrder(this);
    }

    public void tick() {
        age += 1.0;
    }

    public String toString() {
        String status = executed ? " \uD83D\uDFE5[EXEC]" : " \uD83D\uDFE9[OPEN]";
        if (removed) {
            status = " \uD83D\uDFE8[REMOVED]";
        }
        if (invalid) {
            status = " \uD83D\uDFE6[INVALID]";
        }

        return String.format("%s %s → %s: \"%s\"(%.2f) @ $%.2f",
                status, owner.name, type,item.name, item.condition, price);
    }

    public String getDetailedString() {
        return String.format(
            "Order Details:\n" +
            "  ID: %d\n" +
            "  Type: %s\n" +
            "  Owner: %s\n" +
            "  Item: %s\n" +
            "  Condition: %.2f\n" +
            "  Price: %.2f\n" +
            "  Age: %.0f ticks\n" +
            "  Status: %s\n" +
            "  Executed By: %s",
            id, type, (owner != null ? owner.name : "N/A"), item.name, item.condition, price, age,
            executed ? "EXECUTED" : "ACTIVE",
            (executedBy != null ? executedBy.name : "N/A")
        );
    }
}
