package hdbscan;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

import java.util.ArrayList;

public class ClusterHeirarchy {
	
	private Cluster root;
	private ArrayList<Integer> bestClusters = new ArrayList();
	
	public ClusterHeirarchy(Cluster root){
		this.root = root;
	}
	
	public void makeHeirarchy(){
		calculateClusterHeirarchy(root);
	}
	
	public Double calculateClusterHeirarchy(Cluster cluster){
		Cluster currCluster = cluster;
		System.out.println("Current Cluster: " + currCluster.getLabel());
//		System.out.println("Analyzing: " + currCluster.getLabel());
//		int count = 0;
//		for(DefaultWeightedEdge e : currCluster.getGraph().edgeSet()){
//			System.out.println(currCluster.getGraph().getEdgeWeight(e));
//			count += 1;
//			if (count > 10) break;
//		}
		Double stability = currCluster.analyzeCluster();
		System.out.println(currCluster.getLabel() + " Children: " + (currCluster.hasChildren() ? currCluster.getLeft().getLabel() + " " + currCluster.getRight().getLabel(): "None None"));
		if(currCluster.hasChildren()){
			Double leftStability = calculateClusterHeirarchy(currCluster.getLeft());
			Double rightStability = calculateClusterHeirarchy(currCluster.getRight());

			if (leftStability + rightStability < stability){
				System.out.println("Union: Parent " + currCluster.getLabel() + ":" + currCluster.getStability() + " " +
						currCluster.getLeft().getLabel() + ":" + currCluster.getLeft().getStability() + " " + currCluster.getRight().getLabel() +
						":" + currCluster.getRight().getStability());
				currCluster.labelVertices();
				currCluster.setStability(stability + leftStability + rightStability);
			}else{
				System.out.println("Dissolve: Parent " + currCluster.getLabel() + ":" + currCluster.getStability() + " Children " + currCluster.getLeft().getLabel() +
						":" + currCluster.getLeft().getStability() + " " + currCluster.getRight().getLabel() + ":" + currCluster.getRight().getStability());
				SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> tempGraph = currCluster.getLeft().getGraph();
				Graphs.addGraph(tempGraph,currCluster.getRight().getGraph());
				for (ClusterNode parentNode : currCluster.getGraph().vertexSet()){
					if(!tempGraph.containsVertex(parentNode)){

						for(DefaultWeightedEdge pe : currCluster.getGraph().edgesOf(parentNode)){
//							System.out.println("Adding edge from parent graph " + currCluster.getLabel());
							ClusterNode pV1 = currCluster.getGraph().getEdgeSource(pe);
							ClusterNode pV2 = currCluster.getGraph().getEdgeTarget(pe);
							Double pw = currCluster.getGraph().getEdgeWeight(pe);
//							System.out.println(pV1 + " ->" + pV2 + " | " + pw);
							tempGraph.addVertex(pV1);
							tempGraph.addVertex(pV2);
							pe = tempGraph.addEdge(pV1, pV2);
							tempGraph.setEdgeWeight(pe,pw);

						}
					}
				}
				currCluster.setGraph(tempGraph);
				currCluster.setStability(leftStability + rightStability);
			}
//			count = 0;
//			for (DefaultWeightedEdge e : currCluster.getGraph().edgeSet()){
//				count += 1;
//				if (count > 10) break;
//				System.out.println(currCluster.getGraph().getEdgeSource(e) + "->" + currCluster.getGraph().getEdgeTarget(e));
//			}
		}
		root = currCluster;
		return currCluster.getStability();
	}

	public Cluster getRoot() {
		return root;
	}
	

}
