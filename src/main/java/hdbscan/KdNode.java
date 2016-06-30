package hdbscan;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.alg.util.UnionFind;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author Damien Manier
 */
public class KdNode implements Comparable<KdNode> {

    private Coordinate p = null;
    private KdNode left;
    private KdNode right;
    private KdNode parent;
    private Integer axis;
    private Double coreDistance;
    private Double bboxDistance; 
	private Integer label;
	private TreeMap<Double, KdNode> neighbors;
	TreeSet<Double> intervals;
	private Integer k;
	private static int nodeCount = 0;


	private Envelope bbox;
	private Envelope prevBbox;
	private boolean hasKNeighbors;

    /**
     * Creates a new KdNode.
     * 
     * @param _x coordinate of point
     * @param _y coordinate of point
     */
    public KdNode(double _x, double _y,int axis, int k) {
        p = new Coordinate(_x, _y);
        left = null;
        right = null;
        this.label = nodeCount;
        nodeCount += 1;
        this.k = k;
        this.axis = axis;
        this.hasKNeighbors = false;
		this.coreDistance = Double.MAX_VALUE;
		this.bboxDistance = Double.MIN_VALUE;
		this.neighbors = new TreeMap<Double,KdNode>();
		intervals = new TreeSet<Double>();
		
		bbox = new Envelope(p,p);
		prevBbox = null;
    }

    /**
     * Creates a new KdNode.
     * 
     * @param p point location of new node
     */
    public KdNode(Coordinate p, int axis, int k) {
        this.p = new Coordinate(p);
        left = null;
        right = null;
        
        this.label = nodeCount;
        nodeCount += 1;
        this.k = k;
        this.axis = axis;
        this.hasKNeighbors = false;
		this.coreDistance = Double.MAX_VALUE;
		this.bboxDistance = Double.MIN_VALUE;
		this.neighbors = new TreeMap<Double,KdNode>();
		intervals = new TreeSet<Double>();
		
		bbox = new Envelope(p,p);
		prevBbox = null;
    }
    
    public void calculateBBox(Double dist){
    	if(neighbors.size() > 0){
	    	final int R = 6371;
	    	final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
	    	final double MAX_LAT = Math.toRadians(90d);   //  PI/2
	    	final double MIN_LON = Math.toRadians(-180d); // -PI
	    	final double MAX_LON = Math.toRadians(180d);  //  PI
	    	double radLat = Math.toRadians(p.y);
	    	double radLon = Math.toRadians(p.x);
	    	
	    	double radDist = dist / R;
	    	
	    	double minLat = radLat - radDist;
	    	double maxLat = radLat + radDist;
	    	
	    	double minLon, maxLon;
	    	if (minLat > MIN_LAT && maxLat < MAX_LAT) {
				double deltaLon = Math.asin(Math.sin(radDist) /
					Math.cos(radLat));
				minLon = radLon - deltaLon;
				if (minLon < MIN_LON) minLon += 2d * Math.PI;
				maxLon = radLon + deltaLon;
				if (maxLon > MAX_LON) maxLon -= 2d * Math.PI;
			} else {
				// a pole is within the distance
				minLat = Math.max(minLat, MIN_LAT);
				maxLat = Math.min(maxLat, MAX_LAT);
				minLon = MIN_LON;
				maxLon = MAX_LON;
			}
	    	prevBbox = bbox;
	    	bbox.expandToInclude(Math.toDegrees(minLon), Math.toDegrees(minLat));
	    	bbox.expandToInclude(Math.toDegrees(maxLon), Math.toDegrees(maxLat));
    	}
    	
    }
    
    public void calculateBBox(){
    	if(bboxDistance != intervals.last()){
    		bboxDistance = calculateMinDistance();
    		if(bboxDistance != null){
    			calculateBBox(bboxDistance);
    		}
    	}
    }
    
    public Double getBboxDistance() {
		return bboxDistance;
	}
    
    public void setBboxDistance(Double bboxDistance){
    	this.bboxDistance = bboxDistance;
    }

	public Double calculateMinDistance(){
    	if(bboxDistance != null){
    		return intervals.higher(bboxDistance);
    	}else{
    		return intervals.first();
    	}
    }
    
    
    public Double addNeighbor(KdNode other){
    	if(neighbors.containsValue(other)){
    		return null;
    	}

		double currDistance = computeDistance(this.p,other.p);
		double retDistance = currDistance;
		
		if(neighbors.size() < k){
			neighbors.put(currDistance,other);
			if(neighbors.size() == k){
				hasKNeighbors = true;
			}
			coreDistance = neighbors.lastEntry().getKey();
			return retDistance;
		}
		else if(currDistance < coreDistance){
			neighbors.pollLastEntry();
			neighbors.put(currDistance,other);
			coreDistance = neighbors.lastEntry().getKey();
			return retDistance;
		} else return null;
		
	}
    
    public Double addNeighbor(KdNode other, double distance){
    	if(neighbors.containsValue(other)){
    		return null;
    	}

		double currDistance = distance;
		double retDistance = currDistance;

		if(neighbors.size() < k){
			neighbors.put(currDistance,other);
			if(neighbors.size() == k){
				hasKNeighbors = true;
			}
			coreDistance = neighbors.lastEntry().getKey();
			return retDistance;
		}
		else if(currDistance < coreDistance){
			neighbors.pollLastEntry();
			neighbors.put(currDistance,other);
			coreDistance = neighbors.lastEntry().getKey();
			return retDistance;
		} else return null;
    }
    
    public void addInterval(KdNode other){
    	if(!this.equals(other)){
	    	Double distance = computeDistance(this.p, other.p);
	    	intervals.add(distance);
	    	addNeighbor(other,distance);
	    	other.addNeighbor(this, distance);
    	}
    }
    
    
    public double computeDistance(Coordinate point1, Coordinate point2){
		final int R = 6371; // Radius of the earth
		
        Double lat1 = point1.y;
        Double lon1 = point1.x;
        Double lat2 = point2.y;
        Double lon2 = point2.x;
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
                   Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * 
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;
        
		return distance;
	}
	
	private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
	
	public int getAxis(){
		return axis;
	}
	
	 /**
     * The value of this node's coordinate along the split dimension.
     * @return this node's split dimension coordinate
     * @since 1.12
     */
    public double getSplitValue() { 
    	double retval ; 
    	switch (axis) { 
    	case 0 :
    		retval = p.x ; 
    		break ;
    	case 1 : 
    		retval = p.y ; 
    		break ; 
    	default :
    		retval = Double.NaN ; 
    	}
    	return retval ;
    }
    

    /**
     * Returns the X coordinate of the node
     * 
     * @retrun X coordiante of the node
     */
    public double getX() {
        return p.x;
    }

    /**
     * Returns the Y coordinate of the node
     * 
     * @return Y coordiante of the node
     */
    public double getY() {
        return p.y;
    }

    /**
     * Returns the location of this node
     * 
     * @return p location of this node
     */
    public Coordinate getCoordinate() {
        return p;
    }

    public double getCoreDistance() {
		return coreDistance;
	}

	public int getLabel() {
		return label;
	}


	public TreeMap<Double, KdNode> getNeighbors() {
		return neighbors;
	}

	public TreeSet<Double> getIntervals() {
		return intervals;
	}

	public int getK() {
		return k;
	}

    /**
     * Returns the left node of the tree
     * 
     * @return left node
     */
    public KdNode getLeft() {
        return left;
    }

    /**
     * Returns the right node of the tree
     * 
     * @return right node
     */
    public KdNode getRight() {
        return right;
    }
    
    public Envelope getBbox() {
		return bbox;
	}
    
    public void setBbox(Envelope bbox){
    	this.bbox = bbox;
    	this.bboxDistance = Double.MAX_VALUE;
    }


	public Envelope getPrevBbox() {
		return prevBbox;
	}

	public boolean hasKNeighbors() {
		return hasKNeighbors;
	}
	
	public boolean isBottom(){
		return (left == null && right == null);
	}

    // Sets left node value
    public void setLeft(KdNode _left) {
        left = _left;
    }

    // Sets right node value
    public void setRight(KdNode _right) {
        right = _right;
    }
    
	public KdNode getParent() {
		return parent;
	}

	public void setParent(KdNode parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "KdNode [p=" + p + ", coreDistance=" + coreDistance + ", label=" + label + ", neighborDistances="
				+ Arrays.toString(neighbors.descendingKeySet().toArray()) + ", bbox=" + bbox + ", hasKNeighbors=" + hasKNeighbors + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KdNode other = (KdNode) obj;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		return true;
	}

	@Override
	public int compareTo(KdNode other) {
		return this.coreDistance < other.coreDistance ? -1 : (this.coreDistance > other.coreDistance ? 1 : 
			(this.label < other.label ? -1 : (this.label > other.label ? 1 : 0)));
	}
}
