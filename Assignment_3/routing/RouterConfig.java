import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;  
import java.io.IOException;

class RouterConfig 
{
    private
        // total list of router
        ArrayList<Router> total_router_list = new ArrayList<Router>();  
        // a adjacency matrix to store all routers and links cost 
        // row = index of source router in total_router_list
        // column = index of dest router in total_router_list
        ArrayList<ArrayList<Integer>> total_link_cost_list = new ArrayList<ArrayList<Integer>>(); 
        int time_step = 0;
    
    public
        // INF -> Unknown link cost
        int INF = 100000;
        Output_transformer output_transformer = new Output_transformer();
        
        // constructor
        RouterConfig(ArrayList<Router> total_router, ArrayList<ArrayList<Integer>> linkCostList) 
        {
            this.total_router_list = total_router;
            this.total_link_cost_list = linkCostList;
        }

        // get total_router_list
        ArrayList<Router> get_total_router() 
        {
            return total_router_list;
        }

        // get total_link_cost
        ArrayList<ArrayList<Integer>> get_total_link_cost() 
        {
            return total_link_cost_list;
        }

        // get time step
        int get_time_step() 
        {
            return time_step;
        }

        // target and return the router from router list(Router type)
        Router getFromRouterList(String routerId) 
        {
            for (int i = 0; i < total_router_list.size(); i++) 
            {
                if (total_router_list.get(i).getId() == routerId) 
                {
                    return total_router_list.get(i);
                }
            }
            return null;
        }

        // get the neighbor routers of targeted source router
        ArrayList<Router> get_neighbor(Router router) 
        {
            ArrayList<Router> neighbor_routers = new ArrayList<Router>();

            int retrieve_index = router.getRouterIndex();

            // the whole row is this router dest routers
            ArrayList<Integer> neighbor_cost = total_link_cost_list.get(retrieve_index);

            // cost != INF is neighbor
            for (int i = 0; i < neighbor_cost.size(); i++) 
            {   
                if (neighbor_cost.get(i) != INF) 
                //if (!neighbor_cost.get(i).equals(INF)) 
                {   
                    neighbor_routers.add(total_router_list.get(i));
                    
                }
            }
            return neighbor_routers;
        }

        // get the destination routers of targeted source router
        ArrayList<Router> get_dest(Router router) 
        {
            ArrayList<Router> dest_routers = new ArrayList<Router>();

            for (int i = 0; i < total_router_list.size(); i++) 
            {   
                if (!(router.getId()).equals(total_router_list.get(i).getId())) 
                {
                    dest_routers.add(total_router_list.get(i));
                }
            }
            return dest_routers;
        }

        // get the neighbor cost list
        ArrayList<Integer> get_cost_from_neighbor(Router router, ArrayList<Router> neighbor)
        {
            ArrayList<Integer> cost = new ArrayList<Integer>();

            for (int i = 0; i < neighbor.size(); i++)
            {
                cost.add(total_link_cost_list.get(router.getRouterIndex()).get(neighbor.get(i).getRouterIndex()));
            }
            return cost;               
        }

        // setter
        // for update the link cost
        void set_link_cost_list(ArrayList<ArrayList<Integer>> new_link_cost_list)
        {
            this.total_link_cost_list = new_link_cost_list;
        }

        // function to perform update of routers and timestep increment
        boolean update_time_step(boolean pr) 
        {
            boolean ifUpdated = false;

            if (time_step != 0)
            {   
                for (int i = 0; i < total_router_list.size(); i++)
                {
                    total_router_list.get(i).update_distanceTable(time_step, pr);
                }
          
                for (int i = 0; i < total_router_list.size(); i++) 
                {
                    if (total_router_list.get(i).update_path_table())
                    {
                        ifUpdated = true;
                    }
                }
            }

            // print routing message here
            for (int i = 0; i < total_router_list.size(); i++)
            {
                output_transformer.print_routing_table(time_step, total_router_list.get(i).getId(), total_router_list.get(i).get_dest_list(), total_router_list.get(i).get_neighbor_list(), total_router_list.get(i).distanceTable);
            }
            
           // update timestep until no update
            if (ifUpdated || (time_step == 0))
            {
                time_step ++;
                return true;

            } else {
                return false;
            }
        }

        // function to perform routers update after link cost change
        void updateRouterCost() 
        {
            for (int i = 0; i < total_router_list.size(); i++)
            {
                ArrayList<Router> nb = total_router_list.get(i).get_neighbor_list();
                ArrayList<Integer> nb_cost = get_cost_from_neighbor(total_router_list.get(i), nb);

                total_router_list.get(i).update_link_cost(nb_cost);
            }
        }

}   