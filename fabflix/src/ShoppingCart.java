import java.util.HashMap;
import java.util.Set;

public class ShoppingCart {

    private final HashMap<String, Integer> cart = new HashMap<String, Integer>();

    public ShoppingCart() {
    }

    public void add(String item, int amount) {
        Integer num_items = this.cart.getOrDefault(item, 0);
        this.cart.put(item, num_items + amount);
        if(this.get(item) <= 0) {
            this.remove(item);
        }
    }

    public Integer get(String item) {
        return this.cart.getOrDefault(item, 0);
    }

    public void remove(String item) {
        this.cart.remove(item);

    }

    public Set<String> getItems() {
        return this.cart.keySet();
    }

    public Integer size() {
        return this.cart.size();
    }


}
