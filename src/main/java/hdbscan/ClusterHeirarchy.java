package hdbscan;

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
		Double stability = currCluster.analyzeCluster();
		if(currCluster.hasChildren()){
			Double leftStability = calculateClusterHeirarchy(currCluster.getLeft());
			Double rightStability = calculateClusterHeirarchy(currCluster.getRight());
			System.out.println("Stability " + currCluster.getLabel() + " " + stability);
			System.out.println("Left " + currCluster.getLeft().getLabel() + " " + leftStability);
			System.out.println("Right " + currCluster.getRight().getLabel() + " " + rightStability);
			if (leftStability + rightStability <= stability){
				System.out.println("Union");
				currCluster.labelVertices();
				currCluster.setStability(stability + leftStability + rightStability);
			}else{
				System.out.println("Dissolve parent");
				currCluster.labelVertices(currCluster.getLeft());
				currCluster.labelVertices(currCluster.getRight());
				System.out.println(currCluster.getLeft().getLabel());
				System.out.println(currCluster.getRight().getLabel());
			}
		}
		int count = 0;
		for (ClusterNode node : currCluster.getGraph().vertexSet()){
			count += 1;
			if(count > 10) break;
			System.out.println(node);
		}
		root = currCluster;
		return stability;
	}

	public Cluster getRoot() {
		return root;
	}
	

}
