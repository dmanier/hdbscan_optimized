package hdbscan;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

import hdbscan.HDBSCAN;
import hdbscan.KdNode;
import hdbscan.NearestKdTree;;

public class HdbscanTest {
	
	public KdNode[] simpleKNN(Coordinate[] data, int k){
		HashSet<KdNode> nodeSet = new HashSet();
		for(int i = 0; i < data.length; i++){
			Coordinate point = data[i];
			point.x = Math.round(point.x / 0.001) / (1/0.001);
			point.y = Math.round(point.y / 0.001) / (1/0.001);
			nodeSet.add(new KdNode(point,0,k));
		}
		KdNode[] nodeArray = new KdNode[nodeSet.size()];
		nodeSet.toArray(nodeArray);
		for(int i=0;i<nodeArray.length;i++){
			for(int j=i+1;j<nodeArray.length;j++){
				nodeArray[i].addNeighbor(nodeArray[j]);
				nodeArray[j].addNeighbor(nodeArray[i]);
			}
		}
		return nodeArray;
	}

	@Test
	public void testKNN() {
		try {
			Coordinate[] data = HDBSCAN.readInDataSet("data/example_data_set.csv", ",");
			KdNode[] nodeArray = simpleKNN(data,10);
			NearestKdTree tree = HDBSCAN.calculateNearestKdTree(data, 10, 0.001);
			ArrayList<KdNode> treeArray = tree.getAllNodes();
			for(KdNode n1 : nodeArray){
				for(KdNode n2 : treeArray){
					if(n1.equals(n2)){
						System.out.println(n1.getNeighbors().toString());
						System.out.println(n2.getNeighbors().toString());
					}
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
