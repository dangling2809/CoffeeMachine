package coffeemachine;

import com.dunzo.coffeemachine.model.Inventory;
import com.dunzo.coffeemachine.model.Item;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoffeeMachineImpl implements CoffeeMachine{

    private Inventory inventory;

    private int outlets;

    Semaphore outlet = null;

    public CoffeeMachineImpl(Inventory inventory,int outlets) {
        this.inventory = inventory;
        this.outlets=outlets;
        this.outlet =new Semaphore(outlets);
    }

    /**
     * This method checks {@link Inventory} and prepares input Beverage if {@link com.dunzo.coffeemachine.model.Ingredient} are available in RequiredQuantity
     * @param beverageToPrepare
     */
    @Override
    public void prepare(Item beverageToPrepare){
        boolean isItemPrepared= true;
        try {
            //ensure outlet is locked when serving beverage out of it
            outlet.acquire();
            //check if all the ingredients are available to prepare provided beverage
            if(allItemsAvailable(beverageToPrepare)) {
                //if yes check quantity and update in inventory
                for (Map.Entry<String, Integer> entry : beverageToPrepare.getRequiredIngredientQtyMap().entrySet()) {
                    String requiredItem = entry.getKey();
                    Integer requiredQty = entry.getValue();
                    Item itemFromInventory = inventory.getItem(requiredItem);
                    if (itemFromInventory != null) {
                        int oldValue=itemFromInventory.getQty().get();
                        //try to Atomically update if required quantity is less than available qty
                        itemFromInventory.getQty().updateAndGet(qty-> (qty - requiredQty) >= 0 ? (qty - requiredQty) : qty);
                        //if quantity is unchanged then we were not able to update due to less quantity
                        if ( itemFromInventory.getQty().get() == oldValue) {
                            System.out.println(MessageFormat.format(MessageTemplates.ITEM_QTY_NOT_AVAILABLE_TEMPLATE, beverageToPrepare.getName(), requiredItem,
                                    itemFromInventory.getQty()));
                            isItemPrepared=false;
                            break;
                        }
                    } else {
                        // TO ensure Failure message if some other thread has removed item from Inventory after our first Check
                        System.out.println(MessageFormat.format(MessageTemplates.ITEM_NOT_AVAILABLE_TEMPLATE, beverageToPrepare.getName(), requiredItem));
                        isItemPrepared=false;
                        break;
                    }
                }
            }else{
                isItemPrepared=false;
            }
        } catch (InterruptedException e) {
            isItemPrepared=false;
            e.printStackTrace();
        }finally {
            //release always for other thread to work upon
            outlet.release();
           // System.out.println(Thread.currentThread().getName() + " Outlet released");
        }
        if(isItemPrepared)
            System.out.println(MessageFormat.format(MessageTemplates.ITEM_IS_PREPARED_TEMPLATE, beverageToPrepare.getName()));
    }

    /**
     * Check all items needed to prepare beverage are available , Cannot do this check along with Quantity because this check takes priority
     * over items for which quantity is not available.
     * @param beverageToPrepare
     * @return
     */
    private boolean allItemsAvailable(Item beverageToPrepare) {
        AtomicBoolean allItemsAvailable= new AtomicBoolean(true);
        for (String requiredItem : beverageToPrepare.getRequiredIngredientQtyMap().keySet()) {
            if (!inventory.itemAvailable(requiredItem)) {
                System.out.println(MessageFormat.format(MessageTemplates.ITEM_NOT_AVAILABLE_TEMPLATE, beverageToPrepare.getName(), requiredItem));
                allItemsAvailable.set(false);
                break;
            }
        }
        return allItemsAvailable.get();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
