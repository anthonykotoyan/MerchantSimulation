import java.util.Arrays;

public class ValuationChart {

    public static double buyWeight = 2,
                   sellWeight = .5,
                   historicalWeight = 1;

    public static double[] TYPE_PRICES;

    public static final double DEFAULT_PRICE = 10.0;

    public static void setTypePrices() {

        if (Item.ITEM_NAMES != null && (TYPE_PRICES == null || TYPE_PRICES.length != Item.ITEM_NAMES.length)) {
            TYPE_PRICES = new double[Item.ITEM_NAMES.length];
        }
        if (TYPE_PRICES != null) {
            Arrays.fill(TYPE_PRICES, DEFAULT_PRICE);
        }
    }

    public static void updateTypePrice(int itemType, Order.Type orderType, double price, boolean isPlaced) {
        if (TYPE_PRICES == null || itemType >= TYPE_PRICES.length) return;

        double valueWeight = orderType == Order.Type.BUY ? buyWeight : sellWeight;
        double placedBias = isPlaced ? 3.0 : 1.0;
        TYPE_PRICES[itemType] = (TYPE_PRICES[itemType] * historicalWeight*placedBias + (price) * valueWeight) / (historicalWeight*placedBias + valueWeight);

    }

    public static double getPrice(int type) {
        if (TYPE_PRICES == null || type >= TYPE_PRICES.length) return DEFAULT_PRICE;
        return TYPE_PRICES[type];
    }

}
