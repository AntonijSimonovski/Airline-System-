import java.util.*;
import java.io.*;

public class AirlineSystem implements AirlineInterface{
  private ArrayList<String> cityNames;
  private Digraph G = null;
  private static Scanner scan = null;
  private static final int INFINITY = Integer.MAX_VALUE;

  public boolean loadRoutes(String fileName){
    Scanner fileScan;
    try{
      fileScan = new Scanner(new FileInputStream(fileName));
    }
    catch(FileNotFoundException e){
      return false;
    }
    int v = Integer.parseInt(fileScan.nextLine());
    G = new Digraph(v);

    cityNames = new ArrayList<String>();

    for(int i=0; i<v; i++){
      cityNames.add(fileScan.nextLine());
    }
    while(fileScan.hasNextLine()){
      int from = fileScan.nextInt();
      int to = fileScan.nextInt();
      int distance = fileScan.nextInt();
      double price = fileScan.nextDouble();
      G.addEdge(new Route(cityNames.get(from-1), cityNames.get(to-1), distance, price));
      G.addEdge(new Route(cityNames.get(to-1), cityNames.get(from-1), distance, price));
      if(!fileScan.hasNextLine()) break;
      fileScan.nextLine();
    }
    fileScan.close();
    return true;
  }

  public Set<String> retrieveCityNames(){
    HashSet<String> cityNamesSet = new HashSet<>(cityNames);
    return cityNamesSet;
  }

  public Set<Route> retrieveDirectRoutesFrom(String city){
    HashSet<Route> routes = new HashSet<>();
      for (Route e : G.adj(cityNames.indexOf(city))) {
        routes.add(e);
    }
    return routes;
  }

  public Set<ArrayList<String>> fewestStopsItinerary(String source, String destination)throws CityNotFoundException{
    
    HashSet<ArrayList<String>> set = new HashSet<ArrayList<String>>();
    ArrayList<String> list = new ArrayList<>();

    G.bfs(cityNames.indexOf(source));
    if(!G.marked[cityNames.indexOf(destination)]){
      return null;
    } else{
      Stack<Integer> path = new Stack<>();
        for (int x = cityNames.indexOf(destination); x != cityNames.indexOf(source); x = G.edgeTo[x])
            path.push(x);
        path.push(cityNames.indexOf(source));

        while(!path.empty()){
         list.add(cityNames.get(path.pop()));
        }
    }
    set.add(list);
    return set;
  }

  public Set<ArrayList<Route>> shortestDistanceItinerary(String source, String destination){
    HashSet<ArrayList<Route>> set = new HashSet<ArrayList<Route>>();
    ArrayList<Route> list = new ArrayList<>();

    G.dijkstras(cityNames.indexOf(source), cityNames.indexOf(destination));
    if(!G.marked[cityNames.indexOf(destination)]) return null;
    else{
      Stack<Integer> path = new Stack<>();
          for (int x = cityNames.indexOf(destination); x != cityNames.indexOf(source); x = G.edgeTo[x]){
              path.push(x);
          }
          int prevVertex = cityNames.indexOf(source);
          while(!path.empty()){
            int v = path.pop();
            
            for(Route e : G.adj(prevVertex)){
              if(e.destination == cityNames.get(v)) list.add(e);
            }
            prevVertex = v;
          }
    }
    set.add(list);
    return set;
  }

  public Set<ArrayList<Route>> cheapestItinerary(String source, String destination){
    HashSet<ArrayList<Route>> set = new HashSet<ArrayList<Route>>();
    ArrayList<Route> list = new ArrayList<>();

    G.dijkstrasDouble(cityNames.indexOf(source), cityNames.indexOf(destination));
    if(!G.marked[cityNames.indexOf(destination)]) return null;
    else{
      Stack<Integer> path = new Stack<>();
          for (int x = cityNames.indexOf(destination); x != cityNames.indexOf(source); x = G.edgeTo[x]){
              path.push(x);
          }
          int prevVertex = cityNames.indexOf(source);
          while(!path.empty()){
            int v = path.pop();
            for(Route e : G.adj(prevVertex)){
              if(e.destination == cityNames.get(v)) list.add(e);
            }
            prevVertex = v;
          }
    }
    set.add(list);
    return set;
  }

  public Set<ArrayList<Route>> cheapestItinerary(String source, String transit, String destination){
    HashSet<ArrayList<Route>> set = new HashSet<ArrayList<Route>>();
    ArrayList<Route> list = new ArrayList<>();

    G.dijkstrasDouble(cityNames.indexOf(source), cityNames.indexOf(transit));
    if(!G.marked[cityNames.indexOf(transit)]) return null;
    else{
      Stack<Integer> path = new Stack<>();
          for (int x = cityNames.indexOf(transit); x != cityNames.indexOf(source); x = G.edgeTo[x]){
              path.push(x);
          }
          int prevVertex = cityNames.indexOf(source);
          while(!path.empty()){
            int v = path.pop();
            for(Route e : G.adj(prevVertex)){
              if(e.destination == cityNames.get(v)) list.add(e);
            }
            prevVertex = v;
          }
    }

    G.dijkstrasDouble(cityNames.indexOf(transit), cityNames.indexOf(destination));
    if(!G.marked[cityNames.indexOf(destination)]) return null;
    else{
      Stack<Integer> path = new Stack<>();
          for (int x = cityNames.indexOf(destination); x != cityNames.indexOf(transit); x = G.edgeTo[x]){
              path.push(x);
          }
          int prevVertex = cityNames.indexOf(transit);
          while(!path.empty()){
            int v = path.pop();
            for(Route e : G.adj(prevVertex)){
              if(e.destination == cityNames.get(v)) list.add(e);
            }
            prevVertex = v;
          }
    }
    set.add(list);
    return set;
  }

  public Set<Set<Route>> getMSTs(){
    Set<Set<Route>> MSTs = new HashSet<Set<Route>>();
    for(int i = 0; i < cityNames.size(); i++){
      G.prims(i);
      if(!G.marked[i]) G.prims(i);{
        HashSet<Route> MST = new HashSet<Route>(); 

        for(Route e : G.bestEdge) if(e != null) MST.add(e);

        MSTs.add(MST);
      }
    }
    return MSTs;
  }
  public Set<ArrayList<Route>> tripsWithin(String city, double budget){
    Set<ArrayList<Route>> set = new HashSet<ArrayList<Route>>();
    ArrayList<Route> path = new ArrayList<>();
    boolean[] marked = new boolean[cityNames.size()];
    for(int i = 0; i < marked.length; i++) marked[i] = false;
    marked[cityNames.indexOf(city)] = true;
    set = solve(cityNames.indexOf(city), budget, set, path, 0, marked);
    return set;
  }

  public Set<ArrayList<Route>> tripsWithin(double budget){
    Set<ArrayList<Route>> set = new HashSet<ArrayList<Route>>();
    for(int i = 0; i < cityNames.size(); i++) set.addAll(tripsWithin(cityNames.get(i), budget));
    return set;
  }

  private Set<ArrayList<Route>> solve(int v, double budget, Set<ArrayList<Route>> found, ArrayList<Route> path, double price, boolean[] marked){
    for(Route e: G.adj(v)){
      if(price + e.price <= budget && !marked[cityNames.indexOf(e.destination)]){
        marked[cityNames.indexOf(e.destination)] = true;
        path.add(e);
        ArrayList<Route> copy = new ArrayList<Route>(path);
        found.add(copy);
        price += e.price;
        solve(cityNames.indexOf(e.destination), budget, found, path, price, marked);
        marked[cityNames.indexOf(e.destination)] = false;
        path.remove(path.size()-1);
        price -= e.price;
      }
    }
    return found;
  }



  private class Digraph {
    private final int v;
    private int e;
    private LinkedList<Route>[] adj;
    private boolean[] marked;  // marked[v] = is there an s-v path
    private int[] edgeTo;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo;      // distTo[v] = number of edges shortest s-v path
    private double[] distToDouble;
    private double[] edgeToDouble;
    private Route[] bestEdge;
    /**
    * Create an empty digraph with v vertices.
    */
    public Digraph(int v) {
      if (v < 0) throw new RuntimeException("Number of vertices must be nonnegative");
      this.v = v;
      this.e = 0;
      @SuppressWarnings("unchecked")
      LinkedList<Route>[] temp =
      (LinkedList<Route>[]) new LinkedList[v];
      adj = temp;
      for (int i = 0; i < v; i++)
        adj[i] = new LinkedList<Route>();
    }

    /**
    * Add the edge e to this digraph.
    */
    public void addEdge(Route edge) {
      int from = cityNames.indexOf(edge.source);
      adj[from].add(edge);
      e++;
    }


    /**
    * Return the edges leaving vertex v as an Iterable.
    * To iterate over the edges leaving vertex v, use foreach notation:
    * <tt>for (Route e : graph.adj(v))</tt>.
    */
    public Iterable<Route> adj(int v) {
      return adj[v];
    }

    public void bfs(int source) {
      marked = new boolean[this.v];
      distTo = new int[this.e];
      edgeTo = new int[this.v];

      Queue<Integer> q = new LinkedList<Integer>();
      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      q.add(source);

      while (!q.isEmpty()) {
        int v = q.remove();
        for (Route w : adj(v)) {
          if (!marked[cityNames.indexOf(w.destination)]) {
            edgeTo[cityNames.indexOf(w.destination)] = v;
            distTo[cityNames.indexOf(w.destination)] = distTo[v] + 1;
            marked[cityNames.indexOf(w.destination)] = true;
            q.add(cityNames.indexOf(w.destination));
          }
        }
      }
    }
    public void dijkstras(int source, int destination) {
      marked = new boolean[this.v];
      distTo = new int[this.v];
      edgeTo = new int[this.v];


      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (Route w : adj(current)) {
          if (distTo[current]+w.distance < distTo[cityNames.indexOf(w.destination)]) {
            edgeTo[cityNames.indexOf(w.destination)] = current;
            distTo[cityNames.indexOf(w.destination)] = distTo[current]+w.distance;
          }
        }
        //Find the vertex with minimim path distance
        //This can be done more effiently using a priority queue!
        int min = INFINITY;
        current = -1;

        for(int i=0; i<distTo.length; i++){
          if(marked[i])
            continue;
          if(distTo[i] < min){
            min = distTo[i];
            current = i;
          }
        }
        if(current >= 0){
          marked[current] = true;
          nMarked++;
        } else //graph is disconnected
          break;
      }
    }
    public void dijkstrasDouble(int source, int destination) {
      marked = new boolean[this.v];
      distToDouble = new double[this.v];
      edgeTo = new int[this.v];


      for (int i = 0; i < v; i++){
        distToDouble[i] = INFINITY;
        marked[i] = false;
      }
      distToDouble[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (Route w : adj(current)) {
          if (distToDouble[current]+w.price < distToDouble[cityNames.indexOf(w.destination)]) {
            edgeTo[cityNames.indexOf(w.destination)] = current;
            distToDouble[cityNames.indexOf(w.destination)] = distToDouble[current]+w.price;
          }
        }
        //Find the vertex with minimim path distance
        //This can be done more effiently using a priority queue!
        double min = INFINITY;
        current = -1;

        for(int i=0; i<distToDouble.length; i++){
          if(marked[i])
            continue;
          if(distToDouble[i] < min){
            min = distToDouble[i];
            current = i;
          }
        }
        if(current >= 0){
          marked[current] = true;
          nMarked++;
        } else //graph is disconnected
          break;
      }

    }
    public void prims(int source) {
      marked = new boolean[this.v];
      bestEdge = new Route[this.v];
      distTo = new int[this.v];

      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (Route e : G.adj(current)) {
          if(marked[cityNames.indexOf(e.destination)] == true) continue;
          if (e.distance < distTo[cityNames.indexOf(e.destination)]) {
            bestEdge[cityNames.indexOf(e.destination)] = e;
            distTo[cityNames.indexOf(e.destination)] = e.distance;
          }
        }
        for(Route e: bestEdge) if(e != null ) System.out.println(bestEdge[cityNames.indexOf(e.destination)]);
        System.out.println();
        
        //Find the vertex with minimim path distance
        //This can be done more effiently using a priority queue!
        int min = INFINITY;
        current = -1;

        for(int i=0; i<distTo.length; i++){
          if(marked[i])
            continue;
          if(distTo[i] < min){
            min = distTo[i];
            current = i;
          }
        }
        if(current >= 0){
          marked[current] = true;
          nMarked++;
        } else //graph is disconnected
          break;
      }
    }
    
  }
  //PrimMSTTrace class has been taken and edited from the code handouts provided in Khattab's CS1501 course
  

  }