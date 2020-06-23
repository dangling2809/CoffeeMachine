package coffeemachine;

import coffeemachine.input.Machine;
import com.dunzo.coffeemachine.model.Inventory;
import com.dunzo.coffeemachine.model.Item;
import com.dunzo.coffeemachine.model.ItemInventory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * General Utility class for parsing input files, setting defaults
 */
public class CoffeeMachineAppUtil {
    /**
     * Parses --file arguments and gives back list of files
     * @param args
     * @return
     */
    public  List<String> parseArguments(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("CoffeeMachine").build()
                .defaultHelp(true)
                .description("Provide input files for CoffeMachine testing");

        parser.addArgument("--file").nargs("*").required(true)
                .help("Input file list for Simulating Different Coffee Machine Scenarios");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        return ns.<String> getList("file");
    }

    /**
     * Initializes CoffeeMachine instance based on provided input {@link Machine}
     * @param inputMachine
     * @return
     */
    public CoffeeMachine initialize(Machine inputMachine) {
        Inventory inventory=initializeInventory(inputMachine);
        CoffeeMachine cm=new CoffeeMachineImpl(inventory,inputMachine.getOutlets().getCount_n());
        return cm;
    }

    /**
     * Initializes Inventory for coffee machine on provided input {@link Machine}
     * @param inputMachine
     * @return
     */
    private Inventory initializeInventory(Machine inputMachine) {
        Inventory inventory= new ItemInventory();
        Map<String,Integer> total_items_quantity=inputMachine.getTotal_items_quantity();
        total_items_quantity.forEach((key,value)->
                {
                    Item inventoryItem=Item.createInventoryItem(key,value);
                    //to simulate low running ingredients adding random threshold between 10,30 for each item
                    inventory.addItemWithMinimumThreshold(inventoryItem,getRandomThreshold(10,30));
                }
        );
        return inventory;
    }

    /**
     * Random integer between lower and upper bound
     * @param lower
     * @param upper
     * @return
     */
    public int getRandomThreshold(int lower, int upper) {
        return  (int) (Math.random() * (upper - lower)) + lower;
    }
}
