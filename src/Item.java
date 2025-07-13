public class Item {
    public static Item[] ITEM_POOL;
    public static String[] ITEM_NAMES;
    public static int[] ITEM_TYPE_COUNTS;
    public Order order = null;

    public String name;
    public int type = -1;
    public int id = -1;
    public double condition;



    public Item(String name, int type, int id) {
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
            Item item = new Item(name, type, i);
            ITEM_POOL[i]=(item);
        }
        initTypeCount();
    }

    public static void initTypeCount() {
        ITEM_TYPE_COUNTS = new int[ITEM_NAMES.length];
        for (Item item : ITEM_POOL) {
            if (item != null && item.type >= 0 && item.type < ITEM_TYPE_COUNTS.length) {
                ITEM_TYPE_COUNTS[item.type]++;
            }
        }
    }






}
