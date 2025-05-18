package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dto.Route;
import dto.Station;

public class GraphGenerator {
    private HashMap<Integer, List<Integer>> adjacencyList;

    // Constructor to initialize the adjacency list
    public GraphGenerator() {
        adjacencyList = new HashMap<>();
    }

    // Method to add a new vertex to the graph
    public void addVertex(int vertex)
    {
        adjacencyList.put(vertex, new ArrayList<>());
    }

    // Method to add an edge between two vertices
    public void addEdge(int source, int destination)
    {
        adjacencyList.get(source).add(destination);
    }

    // Method to remove a vertex from the graph
    public void removeVertex(int vertex)
    {
        adjacencyList.remove(vertex);
        // Remove the vertex from the neighbors of other
        // vertices
        for (List<Integer> neighbors :
                adjacencyList.values()) {
            neighbors.remove(Integer.valueOf(vertex));
        }
    }

    // Method to remove an edge between two vertices
    public void removeEdge(int source, int destination)
    {
        adjacencyList.get(source).remove(
                Integer.valueOf(destination));

        // For undirected graph, uncomment below line
        // adjacencyList.get(destination).remove(Integer.valueOf(source));
    }

    // Method to get the neighbors of a vertex
    public List<Integer> getNeighbors(int vertex)
    {
        return adjacencyList.get(vertex);
    }

    // Method to print the graph
    public void printGraph()
    {
        for (HashMap.Entry<Integer, List<Integer> > entry :
                adjacencyList.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (Integer neighbor : entry.getValue()) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }
}
