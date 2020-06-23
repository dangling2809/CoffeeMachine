package coffeemachine;

import coffeemachine.input.CoffeeMachineInputParser;
import coffeemachine.input.IParser;
import coffeemachine.input.Machine;
import com.dunzo.coffeemachine.model.Item;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class to simulate coffee machine
 */
public class CoffeeMachineApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        CoffeeMachineApp app=new CoffeeMachineApp();
        CoffeeMachineAppUtil util=new CoffeeMachineAppUtil();
        //Parse program arguments
        List<String> testFiles= util.parseArguments(args);
        //processing each input one by one
        app.processAllInputFiles(util, testFiles);
    }

    /**
     * Run each input scenario one by one and Show Indicator for low running items after each run.
     * @param util
     * @param testFiles
     * @throws IOException
     * @throws InterruptedException
     */
    private void processAllInputFiles(CoffeeMachineAppUtil util, List<String> testFiles) throws IOException, InterruptedException {
        for (String name : testFiles) {
            String content = new String(Files.readAllBytes(Paths.get(name)));
            IParser<String, Machine> inputParser=new CoffeeMachineInputParser();
            //General input parsing
            System.out.println("Processing input : "+ name);
            Machine machine=inputParser.parse(content);
            //To simulate parallel operations , For now Keep number of threads equal to number of slots available if slots not provided default to 3
            ExecutorService executorService= Executors.newFixedThreadPool(machine.getOutlets()!=null?machine.getOutlets().getCount_n():3);
            System.out.println("Initializing coffee machine");
            CoffeeMachine cm=util.initialize(machine);
            System.out.println("Coffee Machine Initialized");
            processBeverages(executorService,cm,machine.getBeverages());
            executorService.shutdown();
            try {
                //wait for all the operations to finish
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                throw e;
            }
            printLowRunningIngredients(cm);
        }
    }

    private static void printLowRunningIngredients(CoffeeMachine cm) {
        if(cm.getInventory().getLowRunningIngredients()!=null && !cm.getInventory().getLowRunningIngredients().isEmpty()) {
            System.out.println("=============== Low Running Ingredients ==============");
            System.out.println("\t\t Item Name \t\t|\t\t Item Qty");
            cm.getInventory().getLowRunningIngredients().forEach(item -> {
                System.out.println("\t\t"+item.getName()+"\t\t | \t\t"+item.getQty());
            });
            System.out.println("=======================================================");
        }
    }

    private static void processBeverages(ExecutorService executorService, final CoffeeMachine cm, Map<String, Map<String, Integer>> beverages) {
        beverages.forEach((key,value)->{
            Item beverage=Item.createBeveragesItem(key,value);
            executorService.execute(
                    ()->{
                        cm.prepare(beverage);
                    }
            );
        });
    }



}
