package task1;

import java.util.*;
import java.util.Queue;

public class BFS{

    private int size;
    private List<List<List<Integer>>> vertices;
    private Queue<Integer> globalQueue;
    private List<Queue<Integer>> localQueues;

    public List<Queue<Integer>> getLocalQueues() {
        return localQueues;
    }

    public void setLocalQueues(List<Queue<Integer>> localQueues) {
        this.localQueues = localQueues;
    }

    private boolean[] visited;
    private boolean isDone;
    private int counter;
    private int localCounter;
    LinkedList[] l;
    private LinkedList<Integer> q;
    private final Map<Integer,Boolean> map = new HashMap<>();

    public BFS(int size, boolean[] visited, int numberOfProcessors, LinkedList[] l) {
        this.l = l;
        q =  new LinkedList<>();
        this.size = size;
        localQueues = new ArrayList<Queue<Integer>>(numberOfProcessors);
        for (int i = 0; i < numberOfProcessors; i++) {
            localQueues.add(new PriorityQueue<Integer>());
        }
        vertices = new ArrayList<>();
        this.visited = visited;
        isDone = false;
        globalQueue = new PriorityQueue<Integer>();
        Double d = Math.pow(size,3);
        globalQueue.add(d.intValue());
        counter = 0;
        localCounter = 0;
        for (int k = 0; k < size; k++) {
            List<List<Integer>> l2 = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                List<Integer> l3 = new ArrayList<>();
                for (int j = 0; j < size; j++) {
                    l3.add(++localCounter);
                }
                l2.add(l3);
            }
            vertices.add(l2);
        }
    }

    public List<Integer> getNeighbours(int node) {
        List<Integer> neighbours = new ArrayList<>();
        int p = (int) (node % Math.pow(size,2));
        if (p == 1) {

            neighbours.add(node + 1);
            neighbours.add(node + size);
        }
        else if(p == size) {
            neighbours.add(node - 1);
            neighbours.add(node + size);
        }
        else if(p == 0){
            neighbours.add(node - 1);
            neighbours.add(node - size);
        }
        else if(p == Math.pow(size,2) - (size-1)){
            neighbours.add(node + 1);
            neighbours.add(node - size);
        }
        else if(p > 1 && p < size){
            neighbours.add(node + 1);
            neighbours.add(node - 1);
            neighbours.add(node + size);
        }
        else if(p > Math.pow(size,2) - (size-1)){
            neighbours.add(node + 1);
            neighbours.add(node - 1);
            neighbours.add(node - size);
        }
        else if(p % size == 1){
            neighbours.add(node - size);
            neighbours.add(node + size);
            neighbours.add(node + 1);
        }
        else if(p % size == 0){
            neighbours.add(node - size);
            neighbours.add(node + size);
            neighbours.add(node - 1);
        }
        else{
            neighbours.add(node - size);
            neighbours.add(node + size);
            neighbours.add(node - 1);
            neighbours.add(node + 1);
        }

        if(node > 0 && node <= Math.pow(size,2)){
            neighbours.add((int) (node + Math.pow(size,2)));
        }
        else if(node > Math.pow(size,3) - Math.pow(size,2)){
            neighbours.add((int) (node - Math.pow(size,2)));
        }
        else {
            neighbours.add((int) (node - Math.pow(size,2)));
            neighbours.add((int) (node + Math.pow(size,2)));
        }

        return neighbours;
    }


    public void BFtraversal(int v)
    {
        l[Integer.parseInt(Thread.currentThread().getName())].add(v);
        visited[v-1]  =  true;


        while( !l[Integer.parseInt(Thread.currentThread().getName())].isEmpty() )
        {
            while (l[Integer.parseInt(Thread.currentThread().getName())].isEmpty())
            {}
            int k = (int) l[Integer.parseInt(Thread.currentThread().getName())].remove();
            for (int i = 1; i < getNeighbours(k).size(); i++) {
                int node = getNeighbours(k).get(i-1);
                if(!visited[node - 1])
                {
                    l[Integer.parseInt(Thread.currentThread().getName())].add(node);
                    visited[node-1] = true;
                }
            }
        }
    }

    public void BFsearch(int v)
    {

        BFtraversal(v);
        for ( int i = 1; i < Math.pow(size,3); i++ )
        {
            if(!visited[i-1])
            {
                BFtraversal(i);
            }
        }
    }

    public static void main(String[] args)
    {
        long start, finish;
        final int numberOfNodes = 300; //300*300*300
        final int numberOfCore = 4;

        boolean[] visited = new boolean[(int) Math.pow(numberOfNodes,3)];
        for (int i = 0; i < Math.pow(numberOfNodes,3); i++) {
            visited[i] = false;
        }

        LinkedList[] l = new LinkedList[numberOfCore];
        for (int i = 0; i <numberOfCore ; i++) {
            l[i] = new LinkedList<>();
        }
        BFS graph = new BFS(numberOfNodes, visited, numberOfCore,l);


        start = Calendar.getInstance().getTimeInMillis();
        Thread[] processors = new Thread[numberOfCore];
        for (int i = 0; i < numberOfCore; i++) {
            int finalI = (int) (Math.pow(numberOfNodes,3)/ numberOfCore * i + 1);
            processors[i] = new Thread(() -> graph.BFsearch(finalI));
            processors[i].setName(String.valueOf(i));
            processors[i].start();
        }
        for (int i = 0; i < numberOfCore; i++) {
            try {
                processors[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        finish = Calendar.getInstance().getTimeInMillis();
        System.out.println("Parallel Time " + (finish - start));
    }
}