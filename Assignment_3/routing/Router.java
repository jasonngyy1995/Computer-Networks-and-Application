import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;  
import java.io.IOException;

class Router 
{
    private 
        String routerId; // name of a router 
        int routerIndex; // the index of a router 
        ArrayList<Router> neighbor_list = new ArrayList<Router>();// list of neighbors
        ArrayList<Router> dest_list = new ArrayList<Router>(); // list of destinations
        ArrayList<Integer> neighbor_cost = new ArrayList<Integer>(); // list of cost to neighbor
        
        ArrayList<Integer> changed_neighbor_cost = new ArrayList<Integer>(); // list of changed cost to neighbor

        ArrayList<ArrayList<Integer>> distanceTable = new ArrayList<ArrayList<Integer>>(); // the first table to include INF link cost and cost to neighbor

        ArrayList<ArrayList<String>> path_table = new ArrayList<ArrayList<String>>(); // path_table[i][cost/0] -> cost to destination router, path_table[i][via/neighbor router/1] -> neighbor router used to access destination

        ArrayList<String> message_advertiser = new ArrayList<String>(); // if shortest path to dest_list[i] updated, inform neighbor router in with content (cost),(via router)

    public
        // int representation of infinity, 100000 is used instead of integer max value for easier debug
        int INF = 100000;
        Output_transformer output_transformer = new Output_transformer();

        // constructor
        Router (String id, int index) 
        {
            this.routerId = id;
            this.routerIndex = index;
        }
        // get routerId
        String getId() 
        {
            return routerId;
        }

        // get routerIndex
        int getRouterIndex() 
        {
            return routerIndex;
        }

        // get router's neighbor_list
        ArrayList<Router> get_neighbor_list() 
        {
            return neighbor_list;
        }

        // get router's dest_list
        ArrayList<Router> get_dest_list() 
        {
            return dest_list;
        }
        
        // get router's neighbor_cost
        ArrayList<Integer> get_neighbor_cost() 
        {
            return neighbor_cost;
        }

        // get message_advertiser
        ArrayList<String> get_message_advertiser() 
        {
            return message_advertiser;
        }

        // get router index in dest_list
        int get_routerIndex_in_dest(String routerId) 
        {
            for (int i = 0; i < dest_list.size(); i++) 
            {
                if (dest_list.get(i).routerId == routerId) 
                {
                    return i;
                }
            }
            return -1;
        }
        
        // get router index in neighbor_list
        int get_neighborIndex_in_neighbor(String neighborId) 
        {
            for (int i = 0; i < neighbor_list.size(); i++) 
            {
                if (neighbor_list.get(i).routerId == neighborId) 
                {
                    return i;
                }
            }
            return -1;
        }
        
        // set router's id
        void set_routerId(String routerId_) 
        {
            this.routerId = routerId_;
        }

        // set router's index
        void set_routerIndex(int routerIndex_) 
        {
            this.routerIndex = routerIndex_;
        }
    
        // create path_table
        // path_table store router's shortest path to each destinations
        ArrayList<ArrayList<String>> create_path_table() 
        {
            ArrayList<ArrayList<String>> path_table = new ArrayList<ArrayList<String>>();

            for (int i = 0; i < dest_list.size(); i++) 
            {
                ArrayList<String> row = new ArrayList<String>();
                ArrayList<Integer> cost_through_neighbor = distanceTable.get(i);

                int shortest_path = cost_through_neighbor.get(0);
                String path_provider_id = neighbor_list.get(0).routerId;

                // compare the cost (row's value in distance table)
                for (int j = 0; j < cost_through_neighbor.size(); j++) 
                {   
                    // ignore -1
                    if (shortest_path > cost_through_neighbor.get(j) && cost_through_neighbor.get(j) != -1) 
                    {
                        shortest_path = cost_through_neighbor.get(j);
                        path_provider_id = neighbor_list.get(j).routerId;
                    }
                }

                // convert the shortest path from int to string
                String str_shortest_path = String.valueOf(shortest_path);
                
                row.add(str_shortest_path);
                row.add(path_provider_id);

                path_table.add(row);
            }

            return path_table;
        }

        // initialize list and table attributes of a router
        void initialise_lists_and_table(ArrayList<Router> dest, ArrayList<Router> neighbor, ArrayList<Integer> neighborCost) 
        {
            dest_list = dest;
            neighbor_list = neighbor;
            neighbor_cost = neighborCost;

            // at first list 'changed_neighbor_cost' are all 0
            for (int i = 0; i < neighbor_cost.size(); i++) 
            {
                changed_neighbor_cost.add(0);
            }

            // initialize distanceTable
            distanceTable.clear();
            for (int i = 0; i < dest_list.size(); i++) 
            {
                ArrayList<Integer> tmp_row = new ArrayList<Integer>();

                // add neighbor cost
                for (int j = 0; j < neighbor_list.size(); j++) 
                {
                    if (get_routerIndex_in_dest(neighbor_list.get(j).getId()) == i) 
                    {
                        tmp_row.add(neighbor_cost.get(j));

                    } else {
                        tmp_row.add(INF);
                    }
                }
                distanceTable.add(tmp_row);
            }

            // intialize the path_table
            // clean it first
            path_table.clear();
            path_table = create_path_table();

            // initialize the message_advertiser
            message_advertiser.clear();

            // create message for inform neighbor's its shortest path to destination
            for (int i = 0; i < dest_list.size(); i++) 
            {
                String init_message = "";

                if (Integer.parseInt(path_table.get(i).get(0)) != INF) 
                { 
                    init_message = path_table.get(i).get(0) + "," + path_table.get(i).get(1);
                }
                message_advertiser.add(init_message);
            }

        }
        
        // update distance table
        // receive shortest path update from neighbors
        void update_distanceTable(int time_step, boolean poisonedReverse) 
        {   
            for (int i = 0; i < dest_list.size(); i++) 
            {   
                // get the update message from neighbors
                ArrayList<String> message = neighbor_list.get(i).get_message_advertiser();
                
                int cost_to_neighbor = neighbor_cost.get(i);
                
                // if there is change of link cost
                if (changed_neighbor_cost.get(i) != 0) 
                {
                    for (int j = 0; j < dest_list.size(); j++) 
                    {   
                        // if is -1, neighbor_list[i] == destinations[j], which is updated in the previous time step
                        int neighbor_index_in_dest = neighbor_list.get(i).get_routerIndex_in_dest(dest_list.get(j).routerId);

                        // if neighbor found
                        if (neighbor_index_in_dest != -1) 
                        {
                            // update the change
                            if (distanceTable.get(j).get(i) == INF) 
                            {
                                distanceTable.get(j).set(i, INF);
                            
                            // ignore changed_neighbor_cost when it is -1 and distance table index has -1 as value already
                            } else if (distanceTable.get(j).get(i) != -1 && changed_neighbor_cost.get(i) != -1) {
                                distanceTable.get(j).set(i, (distanceTable.get(j).get(i) + changed_neighbor_cost.get(i)));
                               
                            }
                        }
                    }
                    
                    // update next
                    changed_neighbor_cost.set(i, 0);
                }   
                
                for (int j = 0; j < dest_list.size(); j++) 
                {   
                    int neighbor_index_in_dest = neighbor_list.get(i).get_routerIndex_in_dest(dest_list.get(j).routerId);

                    // if is -1, neighbor_list[i] == destinations[j], cost is known so no update provided
                    if (neighbor_index_in_dest != -1) 
                    {   
                        String neighbor_update = message.get(neighbor_index_in_dest);

                        // if length > 0 means update available
                        if (neighbor_update.length() != 0) 
                        {   
                            int commaIndex = neighbor_update.indexOf(",");

                            int shortest_path_by_neighbor;
                        
                             // get the shortest path provided by neighbors
                            shortest_path_by_neighbor = Integer.parseInt(neighbor_update.substring(0, commaIndex));
                            String shortest_path_ROUTER_by_neighbor = neighbor_update.substring(commaIndex+1);
                         
                            int cost_update;

                            // for poisoned reverse, if the shortest provider is via this.router, set the link cost as INF
                            if (shortest_path_ROUTER_by_neighbor.equals(this.routerId) && poisonedReverse == true)
                            {
                                cost_update = INF;
                            } else {
                                cost_update = cost_to_neighbor + shortest_path_by_neighbor;
                            }

                            // set the value inside the specific index if its previous value is not -1
                            if (distanceTable.get(j).get(i) != -1)
                            {
                                distanceTable.get(j).set(i,cost_update);
                            }         
                        }
                    }
                }
            }
        }

        // In time step, for cost change update distanceTable, path_table, message_advertiser
        void update_link_cost(ArrayList<Integer> cost_up_from_neighbor)
        {   
            // if neighbor disconnect, whole column of the disconnect neighbor changes to -1
            for (int i = 0; i < cost_up_from_neighbor.size(); i++)
            {   
                int disconnected_index;
                if (cost_up_from_neighbor.get(i) == -1)
                {   
                    disconnected_index = i;
                    for (int j = 0; j < dest_list.size(); j++)
                    {
                        distanceTable.get(j).set(disconnected_index, -1);
                    }

                }
            }

            // update distanceTable
            for (int i = 0; i < dest_list.size(); i++)
            {
                for (int j = 0; j < neighbor_list.size(); j++)
                {   
                    // if direct neighbor
                    if (get_routerIndex_in_dest(neighbor_list.get(j).getId()) == i)
                    {   
                        int original_cost = neighbor_cost.get(j);
                        
                        // update table if cost changed
                        if (original_cost != cost_up_from_neighbor.get(j))
                        {   
                            // if update is -1, put -1 as cost change, for its neighbor to identify and ignore this link
                            if (cost_up_from_neighbor.get(j) == -1){
                                changed_neighbor_cost.set(j, -1);

                            } else {
                                
                                changed_neighbor_cost.set(j, cost_up_from_neighbor.get(j) - original_cost);
                                
                                neighbor_cost.set(j, cost_up_from_neighbor.get(j));
                                distanceTable.get(i).set(j, neighbor_cost.get(j));
                            }
                        }
                    }
                }
            }

            // update path_table
            path_table = create_path_table();

            // update message_advertiser
            for (int i = 0; i < dest_list.size(); i++)
            {
                message_advertiser.set(i, "");
            }

            // send update to neighbors
            for (int i = 0; i < dest_list.size(); i++)
            {
                int neighbor_to_inform = get_neighborIndex_in_neighbor(dest_list.get(i).getId());
                
                // ignore -1 link cost
                if (neighbor_to_inform != -1 && changed_neighbor_cost.get(neighbor_to_inform) != 0 && changed_neighbor_cost.get(neighbor_to_inform) != -1)
                {
                    String message = path_table.get(i).get(0) + "," + path_table.get(i).get(1);
                    message_advertiser.set(i, message);            
                }
            }

        }

        // update path_table
        // for simultaneous update, update of path table will be performed after all routers update the distance tables
        // then message of previous time step won't affect this time step's update
        boolean update_path_table()
        {
            boolean ifUpdated = false;

            // clean the message_advertiser
            for (int i = 0; i < dest_list.size(); i++)
            {
                message_advertiser.set(i, "");
            }

            // create a path table for comparison of new and old path table
            ArrayList<ArrayList<String>> path_table_COPY = create_path_table();

            for (int i = 0; i < dest_list.size(); i++)
            {
                int original_cost = Integer.parseInt(path_table.get(i).get(0));
                    
                int updated_cost = Integer.parseInt(path_table_COPY.get(i).get(0));
                
                // if cost change, store the update into buffer 
                // store all shortest path into buffer, so neighbor won't miss any information
                if (updated_cost != original_cost)
                {   
                    ArrayList<String> updated_link = path_table_COPY.get(i);
                
                    path_table.set(i, updated_link);

                    String message = updated_link.get(0) + "," + updated_link.get(1);

                    message_advertiser.set(i, message);
                    ifUpdated = true;

                } else {
                    ArrayList<String> updated_link = path_table.get(i);
                
                    String message = updated_link.get(0) + "," + updated_link.get(1);

                    message_advertiser.set(i, message);
                }
            }
            
            // if no more update, return false to stop the update process
            return ifUpdated;
        }

        // for using information of path table to print routing message
        void print_path_table() 
        {
            for (int i = 0; i < path_table.size(); i++)
            {   
                output_transformer.print_routing_message(this.routerId, dest_list.get(i).routerId, path_table.get(i).get(1), path_table.get(i).get(0));  
            }
        }

}
        
        
