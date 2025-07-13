import java.util.Arrays;

public class ValuationChart {

    public static double buyWeight = 2,
                   sellWeight = .5,
                   historicalWeight = 1;

    public static double[] TYPE_PRICES = new double[Item.ITEM_NAMES.length];

    public static final double DEFAULT_PRICE = 10.0;

    public static void setTypePrices() {
        Arrays.fill(TYPE_PRICES, DEFAULT_PRICE);
    }

    public static void updateTypePrice(int itemType, Order.Type orderType, double price, double condition) {
        double valueWeight = orderType == Order.Type.BUY ? buyWeight : sellWeight;
        TYPE_PRICES[itemType] = (TYPE_PRICES[itemType] * historicalWeight + (price - condition*price) * valueWeight) / (historicalWeight + valueWeight);
    }

    public static double getPrice(int type) {
        return TYPE_PRICES[type];
    }

}

