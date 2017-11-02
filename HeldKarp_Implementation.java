import java.util.*;
import java.lang.*;
import java.io.*;

public class HeldKarp_Implementation {


    public static void main (String[] args) {

        //input is in the form of a matrix which can be looked at as a directional graph, where
        //matrix[i][j] is the cost of travelling from i to j
        int n = 4;
        int[][]graph =  {   {0, 2000, 6000, 7000},
                            {3000, 0, 8000, 3000},
                            {5000, 9000, 0, 1000},
                            {8000, 4000, 1000, 0}};

        // Compute and print result
        TravellingSalesmsn(graph, n);
    }

    private static void TravellingSalesmsn(int [][]graph, int n){

        /* we'll implement held-karp algorythm.

           Needed notation #1: Best(x,{s}): the minimum path we can make starting from the origin 1, travelling all cities in the
           set S exactly once, finishin at x (which belongs so s).
           Needed notation #2: d(x->y) is the distance going from city x to city y

            Pseudo code:
            //k denotes the size of the subsets we are checking. the cities are numbered 1 (origin) to n. base case is k = 1
            for x in [2...n]
                Best(x, {x}) = d(1->x)

            for k in [2...(n-1)]
                for all subests S in [2.....n] of size K
                    for all x in S: Best(x, {S}) = Min for all m in S where m!=x of [Best(m, {S - {x}}) + d(m->x)]

            opt = Min for all x in [2...n] of (Best(x, {2...n}) + d(x->1))
            return opt

        */
        int k, num_of_subsets;
        ArrayList<HeldKarp_SubSolution> sub_solutions = new ArrayList<HeldKarp_SubSolution>();

        //base case
        k = 1;
        for (int i = 2; i <= n; i++){
            HashSet<Integer> s = new HashSet<Integer>();
            s.add(i);
            sub_solutions.add(new HeldKarp_SubSolution(i, s, Distance(graph, 1, i)));
        }


        //develope the solutions by growing k
        ArrayList<HashSet<Integer>> subsets = new ArrayList<HashSet<Integer>>();  //at each step we can know the size so we can have an array of size (n over k), but array list is simpler for lookup, and we can have the same DS for all steps
        for (k = 2; k <= n - 1; k++){
            CreateAllSubsets(n, k, subsets);
            Iterator<HashSet<Integer>> set_iterator = subsets.iterator();
            while (set_iterator.hasNext()){
                HashSet<Integer> s = set_iterator.next();
                Iterator<Integer> element_iterator = s.iterator();
                while (element_iterator.hasNext()){
                    int x = element_iterator.next();
                    HeldKarp_SubSolution sub_solution = ComputeBestSubset(x, s, sub_solutions, graph);
                    sub_solutions.add(sub_solution);
                }
            }
            subsets.clear(); //after each step, the subsets of size k were used to generate all HeldKarp_SubSolutions, and the array list can be cleared for the next stage
            //All the HeldKarp_SubSolutions objects of size k-1 should be cleareed as well
            if (k > 1){
                Iterator<HeldKarp_SubSolution> sub_solution_iterator = sub_solutions.iterator();
                while (sub_solution_iterator.hasNext()){
                    HeldKarp_SubSolution sub_solution = sub_solution_iterator.next();
                    if (sub_solution.cities.size() == k - 1)
                        sub_solution_iterator.remove();
                }
            }
        }

        //compute and print final result
        int min_solution = Integer.MAX_VALUE;
        int min_distance;
        for (int i = 2; i <= n; i++){
            min_distance = GetFinalMinDistance(i, sub_solutions, graph);
            if (min_distance < min_solution)
                min_solution = min_distance;
        }
        System.out.println(min_solution);
    }


    /* 'a' and 'b' are in the range [1...n] for easier understanding of the pseudo code, so need to substract one */
    private static int Distance(int[][] arr, int a, int b){
        return arr[a - 1][b - 1];
    }

    /* Add to 'subsets' all the subsets in the set [2...n] of size k */
    private static void CreateAllSubsets(int n, int k, ArrayList<HashSet<Integer>> subsets){

        //generate the set [2...n] as an array. This can be done only once in the calling function, but easier to understand when here
        int[] set = new int[n - 1];
        for (int i = 0; i < n - 1; i++)
            set[i] = i + 2;

        CreateAllSubsetsAux(set, n - 1, k, new HashSet<Integer>(), 0, 0, subsets);
    }

    @SuppressWarnings("unchecked")
    private static void CreateAllSubsetsAux(int[] set, int set_size, int k, HashSet<Integer> subset, int subset_size,
                                            int set_index, ArrayList<HashSet<Integer>> all_subsets) {
        //base case 1: we have filled up our subset
        if (subset_size == k) {
            all_subsets.add((HashSet<Integer>)subset.clone());
            return;
        }

        //base case 2: we have run out of elements to add to our set
        if (set_index == set_size)
            return;

        //Continue building a set that contains this element
        subset.add(set[set_index]);
        CreateAllSubsetsAux(set, set_size, k, subset, subset_size + 1 , set_index + 1, all_subsets);

        //Continue building a set that does not contain this element
        subset.remove(set[set_index]);
        CreateAllSubsetsAux(set, set_size, k, subset, subset_size, set_index + 1, all_subsets);
    }

    /*  Compute the minimal path that starts at 1, traverses all the cities in S and ends in x, using the pre-calculated sub-solutions
        Use the following part of the pseudo code:
        Best(x, {S}) = Min for all m in S where m!=x of [Best(m, {S - {x}}) + d(m->x)] */
    @SuppressWarnings("unchecked")
    private static HeldKarp_SubSolution ComputeBestSubset(int x, HashSet<Integer> s, ArrayList<HeldKarp_SubSolution> sub_solutions, int[][]graph){

        HashSet<Integer> subest_without_destination = (HashSet<Integer>)s.clone();
        subest_without_destination.remove(x);
        Iterator<Integer> element_iterator = subest_without_destination.iterator();

        int shortest_path = Integer.MAX_VALUE;

        while (element_iterator.hasNext()){
            int m = element_iterator.next();
            Iterator<HeldKarp_SubSolution> sub_solution_iterator = sub_solutions.iterator();
            boolean found = false;
            while (sub_solution_iterator.hasNext()){
                HeldKarp_SubSolution sub_solution = sub_solution_iterator.next();
                if ((m == sub_solution.last_visited_city) && (subest_without_destination.equals(sub_solution.cities))){
                    int distance = sub_solution.shortest_path + Distance(graph, m, x);
                    if (distance < shortest_path)
                        shortest_path = distance;
                    break;
                }
            }
        }
        return new HeldKarp_SubSolution(x, s, shortest_path);
    }

    /*  Compute the minimal path that starts at 1, traverses all the cities, and ends in x
        Use the following part of the pseudo code:
        (Best(i, {2...n}) + d(i->1) */
    private static int GetFinalMinDistance(int x, ArrayList<HeldKarp_SubSolution> sub_solutions, int [][]graph){
        int ans = -1;
        Iterator<HeldKarp_SubSolution> sub_solution_iterator = sub_solutions.iterator();
        while (sub_solution_iterator.hasNext()){
            HeldKarp_SubSolution sub_solution = sub_solution_iterator.next();
            if (sub_solution.last_visited_city == x) //no need to check the hashset - at this point there should be only 1 HeldKarp_SubSolution ending in x
                ans = sub_solution.shortest_path + Distance(graph, x, 1);
        }
        return ans;
    }

    private static void PrintAllSubsets(ArrayList<HashSet<Integer>> subsets, int k){
        System.out.print("K=" + k + ":" );
        for (HashSet<Integer> subset : subsets){
            Iterator<Integer> element_iterator = subset.iterator();
            System.out.print("{ ");
            while (element_iterator.hasNext()){
                int x = element_iterator.next();
                System.out.print(x + " ");
            }
            System.out.print("} ");
        }
        System.out.println();
    }
}


