import java.security.InvalidAlgorithmParameterException;
import java.io.*;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.*;


/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * @author Rithika Saravanan
 * @author Brandon Fain
 * @author Owen Astrachan modified in Fall 2023
 *
 */



public class GraphProcessor {
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */

    // include instance variables here
    private HashMap<Point, Set<Point>> map; 

    public GraphProcessor() {
        // TODO initialize instance variables
        map = new HashMap<>();

    }

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws IOException if file not found or error reading
     */

     
    public void initialize(FileInputStream file) throws IOException {
        Scanner reader = new Scanner(file);
        String[] fLine = reader.nextLine().split(" ");

        int numVertices = Integer.parseInt(fLine[0]);
        int numEdges = Integer.parseInt(fLine[1]);

 

        String[] vertexNames = new String[numVertices];
        Point[] vertices = new Point[numVertices];

 

        for (int i = 0; i < numVertices; i++) {

            if (!reader.hasNextLine()) {
                reader.close();
                throw new IOException("Could not read .graph file");
            }

            String[] line = reader.nextLine().split(" ");
            vertexNames[i] = line[0];

            Point vertex = new Point(Double.parseDouble(line[1]), Double.parseDouble(line[2]));

            map.put(vertex, new HashSet<>());
            vertices[i] = vertex;

        }

        for (int i = 0; i < numEdges; i++) {
            if (!reader.hasNextLine()) {
                reader.close();
                throw new IOException("Could not read .graph file");

            }

            String[] edge = reader.nextLine().split(" ");

            Point v1 = vertices[Integer.parseInt(edge[0])];
            Point v2 = vertices[Integer.parseInt(edge[1])];

            map.get(v1).add(v2);
            map.get(v2).add(v1);

        }

        reader.close();

    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return list of all vertices in graph
     */

    public List<Point> getVertices(){
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return all edges in graph
     */
    public List<Point[]> getEdges(){
        return null;
    }

    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p is a point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        // TODO implement nearestPoint
        Point near = null;
        double min = Double.MAX_VALUE;

        for(Point x : map.keySet())
        {
            if(p.distance(x) < min){
                near = x;
                min = p.distance(x);
            }
        }

        return near;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double d = 0.0;
        // TODO implement routeDistance
        Point begPoint = route.get(0);
        for(Point p : route)
        {
            d += begPoint.distance(p);
            begPoint = p;
        }
        return d;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if and onlyu if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {

        if (map.keySet().contains(p1) && map.keySet().contains(p2)) {

            Set<Point> already = new HashSet<>();
            Stack<Point> nowAt = new Stack<>();

            nowAt.add(p1);

            while (!nowAt.isEmpty()) {
                Point current = nowAt.pop();
                for (Point p : map.get(current)) {
                    if (p.equals(p2)) return true;
                    if (!already.contains(p)) {
                        already.add(p);
                        nowAt.push(p);
                    }
                }
            }
        }
        return false;
    }

 

    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws IllegalArgumentException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws IllegalArgumentException {
        // TODO implement route
        if (!connected(start, end) || start.equals(end)) {

            throw new IllegalArgumentException("No path between start and end");
        }
        List<Point> quickest = new LinkedList<>();
        quickest.add(end);
        Map<Point, Point> bfsOutput = bfs(start);

        while(!start.equals(end))
        {
            end = bfsOutput.get(end);
            quickest.add(0, end);
        }
        return quickest;
    }

        private Map<Point, Point> bfs(Point beg)
        {
            Map<Point, Double> dist = new HashMap<>();
            Comparator<Point> comp = (x,y) -> (int) (dist.get(x) - dist.get(y));
            PriorityQueue<Point> pq = new PriorityQueue<>(comp);

            dist.put(beg, 0.0);
            pq.add(beg);

            Map<Point, Point> res = new HashMap<>();

            while(!pq.isEmpty()) {
                beg = pq.remove();
                for(Point p : map.get(beg)) {
                    double already = beg.distance(p);
                    if(!dist.containsKey(p) || already + dist.get(beg) < dist.get(p)) {
                        res.putIfAbsent(p, beg);
                        dist.putIfAbsent(p, already + dist.get(beg));
                        pq.add(p);
                    }
                }
            }
            return res;
        }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("Running GraphProcessor");
    }


    
}
