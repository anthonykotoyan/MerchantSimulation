public class Item {
    public static Item[] ITEM_POOL;
    public static String[] ITEM_NAMES;
    public Order order = null;

    public String name;
    public int type = -1;
    public int id = -1;
    public double condition;

    public Item(String name,double condition, int type, int id) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.condition = Math.random();
    }



    public static void createItemPool(int size, int numItems) {
        ITEM_POOL = new Item[size];
        ITEM_NAMES = new String[size];
        for (int i = 0; i < numItems; i++) {
            ITEM_NAMES[i] = Util.itemNameGenerator();
        }
        for (int i = 0; i < size; i++) {
            int type = (int) (Math.random() * numItems);

            String name = ITEM_NAMES[type];
            double condition = Math.random();
            Item item = new Item(name, condition, type, i);
            ITEM_POOL[i]=(item);
        }
    }






}
