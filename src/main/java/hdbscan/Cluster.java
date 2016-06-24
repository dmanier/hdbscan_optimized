package hdbscan;

import java.util.Arrays;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;


public class Cluster {

	// ------------------------------ PRIVATE VARIABLES ------------------------------
	
	private int label;
	private Double birthLevel;
	private Double deathLevel;
	private Double lowestDeathLevel;
	private int minClSize;
	
	private UndirectedWeightedSubgraph<ClusterNode, DefaultWeightedEdge> graph;
	
	private Double stability;

	private Cluster parent;
	private Cluster left;
	private Cluster right;
	private boolean hasChildren;
	private static int clusterCount = 0;
	


	
	/**
	 * Creates a new Cluster.
	 * @param label The cluster label, which should be globally unique
	 * @param parent The cluster which split to create this cluster
	 * @param birthLevel The MST edge level at which this cluster first appeared
	 * @param numPoints The initial number of points in this cluster
	 */
	public Cluster(Cluster parent, Double birthLevel, int minClSize,
			UndirectedWeightedSubgraph<ClusterNode, DefaultWeightedEdge> graph) {
		clusterCount += 1;
		this.graph = graph;
		this.label = clusterCount;
		this.birthLevel = birthLevel;
		this.deathLevel = null;
		this.minClSize = minClSize;
		
		this.stability = 0.0;
		
		this.parent = parent;
		this.hasChildren = false;
		this.left = null;
		this.right = null;
		
		labelVertices();
	}
	
	public void labelVertices(){
		for(DefaultWeightedEdge edge : graph.edgeSet()){
			graph.getEdgeSource(edge).setCluster(this);
			graph.getEdgeTarget(edge).setCluster(this);
		}
		System.out.println("Change label: " + this.getLabel());
		int count = 0;
		for (DefaultWeightedEdge edge : this.getGraph().edgeSet()){
			count += 1;
			if (count > 10) break;
			System.out.println(edge);
		}
	}
	
	public void labelVertices(Cluster cluster){
		for(ClusterNode node : graph.vertexSet()){
			node.setCluster(cluster);
		}
	}
	
	public Double analyzeCluster(){
		EdgeComparator ec = new EdgeComparator(graph);
		UndirectedWeightedSubgraph<ClusterNode, DefaultWeightedEdge> subGraph = 
				new UndirectedWeightedSubgraph(graph,null,null);
		DefaultWeightedEdge[] sortedEdges = new DefaultWeightedEdge[graph.edgeSet().size()];
		graph.edgeSet().toArray(sortedEdges);
		Arrays.sort(sortedEdges,ec);
		for(int i = sortedEdges.length -1; i >= 0 && deathLevel == null; i--){
			Double currEdgeWeight = graph.getEdgeWeight(sortedEdges[i]);
			ClusterNode v1 = graph.getEdgeSource(sortedEdges[i]);
			ClusterNode v2 = graph.getEdgeTarget(sortedEdges[i]);
			subGraph.removeEdge(sortedEdges[i]);

			stability += (1/currEdgeWeight - 1/this.birthLevel);

			if(subGraph.degreeOf(v1) < 1){
				subGraph.removeVertex(v1);
				continue;
				
			}
			if(subGraph.degreeOf(v2) < 1){
				subGraph.removeVertex(v2);
				continue;
			}
			if(subGraph.vertexSet().size() < minClSize){
				deathLevel = currEdgeWeight;
			}else{
				ConnectivityInspector<ClusterNode, DefaultWeightedEdge> ci = new ConnectivityInspector<>(subGraph);
				if(!ci.isGraphConnected()){
					deathLevel = currEdgeWeight;
					Set<ClusterNode>leftVertices = ci.connectedSetOf(v1);
					Set<ClusterNode>rightVertices = ci.connectedSetOf(v2);
					if(leftVertices.size() >= minClSize && rightVertices.size() >= minClSize){
						left = new Cluster(this,currEdgeWeight,minClSize,
								new UndirectedWeightedSubgraph<>(subGraph, ci.connectedSetOf(v1),subGraph.edgeSet()));
						right = new Cluster(this,currEdgeWeight,minClSize,
								new UndirectedWeightedSubgraph<>(subGraph, ci.connectedSetOf(v2),subGraph.edgeSet()));
						labelVertices(left);
						labelVertices(right);
						hasChildren = true;
					}
				}
			}
		}
		return stability;
	}
	
	public int getLabel() {
		return this.label;
	}
	
	public Cluster getParent() {
		return this.parent;
	}
	

	public Double getDeathLevel() {
		return deathLevel;
	}

	public int getMinClSize() {
		return minClSize;
	}

	public UndirectedWeightedSubgraph<ClusterNode, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	public Cluster getLeft() {
		return left;
	}

	public Cluster getRight() {
		return right;
	}
	
	public void clearChildren(){
		left = null;
		right = null;
		hasChildren = false;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public static int getClusterCount() {
		return clusterCount;
	}

	public Double getBirthLevel() {
		return this.birthLevel;
	}

	public double getStability() {
		return this.stability;
	}
	
	public void setStability(double stability){
		this.stability = stability;
	}
	
	public boolean hasChildren() {
		return this.hasChildren;
	}
}