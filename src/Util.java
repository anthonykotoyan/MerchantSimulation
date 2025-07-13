public class Util {

    static final String[] vowels = {
            "a", "o", "e", "i", "u", "ae", "io", "ea", "y"
    };

    static final String[] consonants = {
            "r", "s", "n", "l", "m", "d", "k", "t", "v", "g",
            "b", "f", "z", "j", "p", "c", "w"
    };

    static final String[] specialConsonants = {
            "th", "dr", "sh", "kr", "gr", "tr", "kh", "gh", "ph", "zh",
            "br", "fr", "sk", "ch", "bl", "gl", "cl", "sn", "qu", "fl", "wh"
    };

    static final String[] specialVowels = {
            "ae", "ia", "ie", "io", "ou", "ei", "oa", "ai", "ea", "ao", "ui", "uu"
    };

    static final String[] endings = {
            "ar", "ion", "or", "eth", "iel", "yn", "in", "an", "on", "en",
            "ax", "us", "is", "as", "ir", "al", "ian", "ys"
    };
    static final String[] joiners = {
            "of", "the", "and", "in", "on", "for", "to", "by", "with", "at",
            "from", "upon", "under", "over", "into", "between", "against", "within"
    };







    public static String nameGenerator(int minLength, int maxLength) {
        int length = (int) (Math.random() * minLength) + maxLength;
        int currLetter = 0;
        StringBuilder name = new StringBuilder();
        boolean isVowel = Math.random() < 0.35;
        double rF = 1.5;
        while (currLetter< length) {
            if (isVowel) {
                if (Math.random() < 0.4 && currLetter > 0) {
                    name.append(specialVowels[(int) (wandom(rF)  * specialVowels.length)]);
                } else {
                    name.append(vowels[(int) (wandom(rF)  * vowels.length)]);
                }
            } else {
                if (Math.random() < 0.2 && currLetter > 0) { // 20% chance to use a special consonant
                    name.append(specialConsonants[(int) (wandom(rF)  * specialConsonants.length)]);
                } else {
                    name.append(consonants[(int) (wandom(rF)  * consonants.length)]);
                }
            }
            isVowel = !isVowel;
            currLetter++;
        }
        // Add an ending
        if (isVowel){
            name.append(endings[(int) (wandom(rF) * endings.length)]);
        }else {
            name.append(consonants[(int) (wandom(rF) * consonants.length)]);
        }
        // Capitalize the first letter
        if (name.length() > 0) {
            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        }
        return name.toString();
    }
    public static void main(String[] args) {

        Item.createItemPool(100, 10);
        for (Item item : Item.ITEM_POOL) {
            System.out.println(item.name + " - Condition: " + item.condition);
        }
    }

    public static String itemNameGenerator(){
        String name = nameGenerator(1,2);
        double chanceForAnotherWord = .25;
        double chanceForJoiner = .25;
        if (Math.random() < chanceForAnotherWord) {
            if (Math.random() < chanceForJoiner) {
                return name + " "+ joiners[(int)(Math.random() * joiners.length)]+" "+ itemNameGenerator();
            }
            return name + " " + itemNameGenerator().toLowerCase();
        } else {
            return name;
        }
    }

    public static double wandom(double factor){
        return Math.pow(Math.random() , factor);
    }


    public static double roundTo(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
