package hdbscan;

import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;


public class Cluster {

	private int label;
	private Double birthLevel;
	private int minClSize;
	
	private SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> graph;
	
	private Double stability;

	private Cluster parent;
	private Cluster left;
	private Cluster right;
	private boolean hasChildren;
	private static int clusterCount = 0;
	


	
	/**
	 * Creates a new Cluster.
	 * @param parent The cluster which split to create this cluster
	 * @param birthLevel The MST edge level at which this cluster first appeared
	 * @param minClSize The minimum num of points to be a cluster
	 * @param graph UndirectedWeightedGraph the represents initital cluster
	 */
	public Cluster(Cluster parent, Double birthLevel, int minClSize,
			SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> graph) {
		clusterCount += 1;
		this.graph = graph;
		this.label = clusterCount;
		this.birthLevel = birthLevel;
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
	}
	
	public void labelVertices(Cluster cluster){
		for (DefaultWeightedEdge edge : graph.edgeSet()){
			graph.getEdgeSource(edge).setCluster(cluster);
			graph.getEdgeTarget(edge).setCluster(cluster);
		}
	}
	
	public Double analyzeCluster(){
		EdgeComparator ec = new EdgeComparator(graph);
		DefaultWeightedEdge[] sortedEdges = new DefaultWeightedEdge[graph.edgeSet().size()];
		HashMap<Pair<ClusterNode,ClusterNode>,Double> removedEdges = new HashMap();
		graph.edgeSet().toArray(sortedEdges);
		Arrays.sort(sortedEdges,ec);
//		System.out.println("Sorting: " + sortedEdges.length);
//		for (int j = sortedEdges.length-1; j >= 0 && j > sortedEdges.length -10; j--){
//			System.out.println(graph.getEdgeWeight(sortedEdges[j]));
//		}
		for(int i = sortedEdges.length -1; i >= 0; i--){
			Double currEdgeWeight = graph.getEdgeWeight(sortedEdges[i]);
			ClusterNode v1 = graph.getEdgeSource(sortedEdges[i]);
			ClusterNode v2 = graph.getEdgeTarget(sortedEdges[i]);
			Double edgeWeight = graph.getEdgeWeight(sortedEdges[i]);
//			System.out.println(getLabel() + " : " + v1 + "->" + v2 + " | " + edgeWeight);
			removedEdges.put(new Pair(v1,v2),edgeWeight);
			graph.removeEdge(sortedEdges[i]);
			if(v1 == null || v2 == null || edgeWeight == null){
				System.out.println("Searched for edge no longer in graph");
				continue;
			}

			boolean prune = false;
			if(!graph.containsVertex(v1)){
				prune = true;
			}else if(graph.degreeOf(v1) < 1){
				graph.removeVertex(v1);
				prune = true;

			}
			if(!graph.containsVertex(v2)){
				prune = true;
			}else if(graph.degreeOf(v2) < 1){
				graph.removeVertex(v2);
				prune = true;
			}
			if(graph.vertexSet().size() < minClSize){
				for(int j = i-1; j> 0; j-- ){
					stability += (1/currEdgeWeight - 1/this.birthLevel);
//					System.out.println(getLabel() + " stability (min cluster) increased to: " + stability + " Current EW: " + currEdgeWeight + " BL: " + birthLevel);
				}
				for (Map.Entry<Pair<ClusterNode, ClusterNode>, Double> entry : removedEdges.entrySet()) {
					graph.addVertex(entry.getKey().getFirst());
					graph.addVertex(entry.getKey().getSecond());
					DefaultWeightedEdge tempEdge = graph.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
					graph.setEdgeWeight(tempEdge, entry.getValue());
				}
				break;
			}else if (prune) {
				stability += (1/currEdgeWeight - 1/this.birthLevel);
//				System.out.println(getLabel() + " stability (prune) increased to: " + stability + " Current EW: " + currEdgeWeight + " BL: " + birthLevel);
				continue;
			}else{
				ConnectivityInspector<ClusterNode, DefaultWeightedEdge> ci = new ConnectivityInspector<>(graph);
				if(!ci.isGraphConnected()){
//					System.out.println("Not Connected");
					Set<ClusterNode>leftVertices = ci.connectedSetOf(v1);
					Set<ClusterNode>rightVertices = ci.connectedSetOf(v2);
//					System.out.println("Component sizes:" + leftVertices.size() + " : " + rightVertices.size());
					if(leftVertices.size() >= minClSize && rightVertices.size() >= minClSize){
						System.out.println("Component sizes:" + leftVertices.size() + " : " + rightVertices.size());
						SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> leftGraph = new SimpleWeightedGraph(DefaultWeightedEdge.class);
						SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> rightGraph = new SimpleWeightedGraph(DefaultWeightedEdge.class);
						for (ClusterNode leftInput : ci.connectedSetOf(v1)) {
							leftGraph.addVertex(leftInput);
						}
						for (ClusterNode rightInput : ci.connectedSetOf(v2)) {
							rightGraph.addVertex(rightInput);
						}
						Double tempBL = 0.0;
						for (ClusterNode node : leftGraph.vertexSet()) {
							for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
								DefaultWeightedEdge le = leftGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
								Double lw = graph.getEdgeWeight(edge);
								if (lw > tempBL) {
									tempBL = lw;
								}
								if (le != null) leftGraph.setEdgeWeight(le, lw);
							}
						}
						left = new Cluster(this, tempBL, minClSize, leftGraph);
						tempBL = 0.0;
						for (ClusterNode node : rightGraph.vertexSet()) {
							for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
								DefaultWeightedEdge re = rightGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
								Double rw = graph.getEdgeWeight(edge);
								if (rw > tempBL) {
									tempBL = rw;
								}
								if (re != null) rightGraph.setEdgeWeight(re, rw);
							}
						}
						right = new Cluster(this, tempBL, minClSize, rightGraph);
//						System.out.println("Right Edges:");
//						for (DefaultWeightedEdge e3 : right.getGraph().edgeSet()){
//							System.out.println(right.getLabel() + " REW: " + right.getGraph().getEdgeWeight(e3));
//						}

						for (Map.Entry<Pair<ClusterNode, ClusterNode>, Double> entry : removedEdges.entrySet()) {
							graph.addVertex(entry.getKey().getFirst());
							graph.addVertex(entry.getKey().getSecond());
							DefaultWeightedEdge tempEdge = graph.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
							graph.setEdgeWeight(tempEdge, entry.getValue());
						}

//						labelVertices(left);
//						labelVertices(right);
						hasChildren = true;
						break;
					}else if(leftVertices.size() < minClSize && rightVertices.size() < minClSize) {
						for(int j = i-1; j> 0; j-- ){
							stability += (1/currEdgeWeight - 1/this.birthLevel);
//							System.out.println(getLabel() + " stability (split death) increased to: " + stability + " Current EW: " + currEdgeWeight + " BL: " + birthLevel);
						}
						for (Map.Entry<Pair<ClusterNode, ClusterNode>, Double> entry : removedEdges.entrySet()) {
							graph.addVertex(entry.getKey().getFirst());
							graph.addVertex(entry.getKey().getSecond());
							DefaultWeightedEdge tempEdge = graph.addEdge(entry.getKey().getFirst(), entry.getKey().getSecond());
							graph.setEdgeWeight(tempEdge, entry.getValue());
						}
						break;

					}else if(leftVertices.size() < minClSize){
						for(ClusterNode lv : leftVertices){
							HashSet<DefaultWeightedEdge> tempEdges = new HashSet(graph.edgesOf(lv));
							for(DefaultWeightedEdge lve : tempEdges){
								stability += (1/currEdgeWeight - 1/this.birthLevel);
//								System.out.println(getLabel() + " stability (prune left small cluster) increased to: " + stability + " Current EW: " + currEdgeWeight + " BL: " + birthLevel);
								ClusterNode ls = graph.getEdgeSource(lve);
								ClusterNode lt = graph.getEdgeTarget(lve);
								Double lw = graph.getEdgeWeight(lve);
								removedEdges.put(new Pair(ls,lt),lw);
								graph.removeEdge(lve);
							}
						}

					}else if(rightVertices.size() < minClSize){
						for(ClusterNode rv : rightVertices){
							HashSet<DefaultWeightedEdge> tempEdges = new HashSet(graph.edgesOf(rv));
							for(DefaultWeightedEdge rve : tempEdges){
								stability += (1/currEdgeWeight - 1/this.birthLevel);
//								System.out.println(getLabel() + " stability (prune right small cluster) increased to: " + stability + " Current EW: " + currEdgeWeight + " BL: " + birthLevel);
								ClusterNode rs = graph.getEdgeSource(rve);
								ClusterNode rt = graph.getEdgeTarget(rve);
								Double rw = graph.getEdgeWeight(rve);
								removedEdges.put(new Pair(rs,rt),rw);
								graph.removeEdge(rve);
							}
						}
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

	public int getMinClSize() {
		return minClSize;
	}

	public SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	public void setGraph(SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> graph){
		this.graph = graph;
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