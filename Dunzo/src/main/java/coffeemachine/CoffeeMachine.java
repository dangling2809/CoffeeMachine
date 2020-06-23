package coffeemachine;

import com.dunzo.coffeemachine.model.Inventory;
import com.dunzo.coffeemachine.model.Item;

public interface CoffeeMachine {

    void prepare(Item beverageToPrepare);

    Inventory getInventory();
}
