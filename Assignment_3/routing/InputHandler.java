import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;  
import java.io.IOException;


public class InputHandler 
{
    // Store all readed router 
    ArrayList<String> total_router = new ArrayList<String>();
    
    // Store all readed link cost 
    ArrayList<String> original_link_cost = new ArrayList<String>();

    // Store the update info if exists
    ArrayList<String> changed_link_cost = new ArrayList<String>();

    // int representation of infinity, 100000 is used instead of integer max value for easier debug
    int INF = 100000;

    // check if two consecutive blank lines exist at the end of input
    int blank_line_count = 0;
    boolean two_blank = false;

    // how many time blank line appear
    int blank_th = 0;

    // if less than 4 blank lines in input, means this input is without update
    int total_blank = 0;
    
    // constructor 
    InputHandler(ArrayList<String> input_list) 
    {   
        // tmp for storing
        ArrayList<String> tmp_total_router = new ArrayList<String>();
        ArrayList<String> tmp_org_link_cost = new ArrayList<String>();
        ArrayList<String> tmp_ch_link_cost = new ArrayList<String>();

        for (int i = 0; i < input_list.size(); i++)
        {   
            if (input_list.get(i).equals("")) 
            {
               total_blank += 1;
            }
        }

        for (int i = 0; i < input_list.size(); i++) 
        {  
            // store each line of input into an ArrayList
            if (input_list.get(i).equals(""))
            {
                blank_th += 1;
                blank_line_count += 1;
            }
            // if e.g. X store all router in a list
            if (input_list.get(i).length() == 1 && blank_th < 2) 
            {
                tmp_total_router.add(input_list.get(i));
                blank_line_count = 0;
            }
            // if e.g. X Y 8  store all the line in a list
            if (input_list.get(i).length() > 1) 
            {
                // 2nd blank line indicates the coming part is changed cost info
                if (blank_th == 2) 
                {
                    tmp_ch_link_cost.add(input_list.get(i));
                } else {
                    tmp_org_link_cost.add(input_list.get(i));
                    blank_line_count = 0;
                }
            }
        }
        
        // two consecutive blank lines at the end of input
        if (blank_line_count == 2) 
        {
            two_blank = true;
        }

        // sort the router list alphabetically
        Collections.sort(tmp_total_router);

        this.total_router = tmp_total_router;
        this.original_link_cost = tmp_org_link_cost;
        this.changed_link_cost = tmp_ch_link_cost;
    }

    // set up a list storing router objects
    ArrayList<Router> create_routerList() 
    {
        ArrayList<Router> routerList = new ArrayList<Router>();
        String router_name;

        // create a router object and store in a list
        for (int i = 0; i < total_router.size(); i++) 
        {
            router_name = total_router.get(i);
            Router router = new Router(router_name, i);
            routerList.add(router);
        }
        return routerList;
    }

    // storing the original information here, create an adjacency matrix to represent the link cost between routers
    ArrayList<ArrayList<Integer>> create_linkCostList() 
    {
        ArrayList<ArrayList<Integer>> linkCostList = new ArrayList<ArrayList<Integer>>();

        String sourceRouter;
        String destRouter;
        int source_index;  // source router index in total_router
        int dest_index;    // dest router index in total_router
        int link_cost;

        for (int i = 0; i < total_router.size(); i++) 
        {
            // initialized cost with INF 
            ArrayList<Integer> init_cost = new ArrayList<Integer>(); 
            for (int j = 0; j < total_router.size(); j++) 
            {
                init_cost.add(INF);
            }
            linkCostList.add(init_cost);
        }

        for (int i = 0; i < original_link_cost.size(); i++) 
        {
            // separate input line into three elements e.g X Y 2
            // X -> source, Y -> dest, 3 -> link cost between
            String[] parts = original_link_cost.get(i).split(" ");
            sourceRouter = parts[0];
            destRouter = parts[1];
            link_cost = Integer.parseInt(parts[2]);

            // retrieve the index
            source_index = total_router.indexOf(sourceRouter);
            dest_index = total_router.indexOf(destRouter);

            // row = source routers, column = destination routers
            linkCostList.get(source_index).set(dest_index, link_cost);
            linkCostList.get(dest_index).set(source_index, link_cost);
        }

        return linkCostList;
    }

    // update linkCostList with the updated link cost information, cost setting logic same as create_linkCostList()
    void update_linkCostList(RouterConfig routerConfig) 
    {
        ArrayList<ArrayList<Integer>> linkCostList = routerConfig.get_total_link_cost();

        String sourceRouter;
        String destRouter;
        int source_index;  // source router index in total_router
        int dest_index;    // dest router index in total_router
        int link_cost;

        for (int i = 0; i < changed_link_cost.size(); i++) 
        {
            // separate input line into three elements e.g X Y 2
            // X -> source, Y -> dest, 3 -> link cost between
            String[] parts = changed_link_cost.get(i).split(" ");
            sourceRouter = parts[0];
            destRouter = parts[1];
            link_cost = Integer.parseInt(parts[2]);

            // retrieve the index
            source_index = total_router.indexOf(sourceRouter);
            dest_index = total_router.indexOf(destRouter);

            // row = source routers, column = destination routers
            linkCostList.get(source_index).set(dest_index, link_cost);
            linkCostList.get(dest_index).set(source_index, link_cost);
        }

        routerConfig.set_link_cost_list(linkCostList);
    }

}
