import java.util.ArrayList;

public class Output_transformer 
{   
    // int representation of infinity, 100000 is used instead of integer max value for easier debug
    int INF = 100000;

    // function to print routing message
    public void print_routing_message(String source, String destination, String via, String cost)
    {   
        System.out.printf("router %s: %s is %s routing through %s\n", source, destination, cost, via);

    }

    // function to print routing table
    public void print_routing_table(int time_step, String source_router_id, ArrayList<Router> dest_list, ArrayList<Router> neighbor_list, ArrayList<ArrayList<Integer>> distanceTable)
    {   
        // Running router's routerId in each time step
        // then print neigbor's routerId
        System.out.printf("\nrouter %s at t = %d \n", source_router_id, time_step);
        for (int i = 0; i < neighbor_list.size(); i++)
        {   
            if (neighbor_list.get(i).getId() != source_router_id)
            {
                System.out.printf("  %s", neighbor_list.get(i).getId());
            }      
        }
        System.out.println("\n");

        // print destination first in each row
        for (int i = 0; i < dest_list.size(); i++)
        {   
           
            System.out.printf("%s ", dest_list.get(i).getId());
              
            // print the distance table
            for (int j = 0; j < distanceTable.size(); j++)
            {   
                if (distanceTable.get(i).get(j) >= INF) 
                {
                    System.out.printf("INF ");
                
                // -1 : disconnected link cost, represented as -
                } else if (distanceTable.get(i).get(j) == -1) {
                    System.out.printf("-  ");

                } else {
                    System.out.printf("%s  ", distanceTable.get(i).get(j));

                }
            }
            System.out.println("\n");
        }
         
    }
}