import java.io.*;  
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PoisonedReverse { 
    public static void main(String[] args) 
    {    
        // store the entire input, each line as an element in an Arraylist
        ArrayList<String> input_list = new ArrayList<String>();

        // start reading input
        
        try 
        {               
            //BufferedReader routerInput = new BufferedReader(new InputStreamReader(System.in));
            File file = new File(args[0]);
            FileReader f_reader = new FileReader(file);
            BufferedReader routerInput = new BufferedReader(f_reader);
            String input_line;
            while ((input_line = routerInput.readLine()) != null)
            {   
                // read line by line and trim whitespace in front and end of the string
                // String input = routerInput.readLine().trim(); 
                input_list.add(input_line);
            }

            routerInput.close();
        
        } catch (IOException e) {
            System.out.println("Incorrect input format, now terminate the program");
            System.exit(1);
        } 
        
        // getting information from input
        InputHandler inputHandler = new InputHandler(input_list);

        ArrayList<Router> total_router_list = inputHandler.create_routerList();

        ArrayList<ArrayList<Integer>> total_link_cost = inputHandler.create_linkCostList();

        RouterConfig routerConfig = new RouterConfig(total_router_list, total_link_cost);

        // Initialization of attributes of each router
        for (int i = 0; i < total_router_list.size(); i++)
        {   
            ArrayList<Router> neighbor_list = routerConfig.get_neighbor(total_router_list.get(i));
            ArrayList<Router> dest_list = routerConfig.get_dest(total_router_list.get(i));
            ArrayList<Integer> cost_to_neighbor = routerConfig.get_cost_from_neighbor(total_router_list.get(i), neighbor_list);

            total_router_list.get(i).initialise_lists_and_table(dest_list, neighbor_list, cost_to_neighbor);

        }

        /* update */

        boolean update = true;

        // continue if update exist
        while (update)
        {   
            update = routerConfig.update_time_step(true); // true = poisoned reverse
        }

        for (int i = 0; i < total_router_list.size(); i++)
        {   
            total_router_list.get(i).print_path_table();
        }
        
        // if the input have 3 or less blank line, no need to update and end the program(input with update has at least 4 blank lines)
        if (inputHandler.total_blank <= 3)
        {
            System.exit(0);
        }

        /* update after link cost change */

        inputHandler.update_linkCostList(routerConfig);
        routerConfig.updateRouterCost();

        update = true;

        routerConfig.time_step += 1;

        while (update)
        {   
            update = routerConfig.update_time_step(true); // true = poisoned reverse
        }
        
        // result
        for (int i = 0; i < total_router_list.size(); i++)
        {
            total_router_list.get(i).print_path_table();
        }  

        // if two consecutive blank lines at the end of input, end the program
        if (inputHandler.two_blank == true)
        {
            System.out.println("Program finished execution, now exit normally");
            System.exit(0);
        }
    }
}
    

