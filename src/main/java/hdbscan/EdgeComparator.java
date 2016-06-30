package hdbscan;

import java.util.Comparator;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

public class EdgeComparator implements Comparator<DefaultWeightedEdge> {
	private SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> graph;
	
	public EdgeComparator(SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> graph){
		this.graph = graph;
	}

	@Override
	public int compare(DefaultWeightedEdge e1, DefaultWeightedEdge e2) {
		Double w1 = graph.getEdgeWeight(e1);
		Double w2 = graph.getEdgeWeight(e2);
		
		if(w1 < w2) return -1;
		else if (w1 > w2) return 1;
		else return 0;
	}

}
