package hdbscan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.jgrapht.alg.util.UnionFind;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.ArrayListVisitor;

public class HDBSCAN {
	
	public static NearestKdTree calculateNearestKdTree(Coordinate[] points,int k,double tolerance){
		NearestKdTree tree = new NearestKdTree(points,k,tolerance);
		tree.findKNN();
		return tree;
		
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> calculateMST(NearestKdTree kdTree){
		SimpleWeightedGraph<ClusterNode,DefaultWeightedEdge> swg = new SimpleWeightedGraph(DefaultWeightedEdge.class);
		
		TreeSet<MutualReachabilityEdge> mrEdges = new TreeSet<MutualReachabilityEdge>();
	
		TreeSet<KdNode> nodes = new TreeSet(kdTree.getAllNodes());
		UnionFind<KdNode> forest = new UnionFind(nodes);
		long count = 0;
		while(swg.edgeSet().size() < nodes.size() - 1){
			for(KdNode node : nodes){
				if(count==0){
					swg.addVertex(new ClusterNode(node));
					for(KdNode neighbor : node.getNeighbors().values()){
						mrEdges.add(new MutualReachabilityEdge(node, neighbor));
					}
				}else if(count==1){
					node.calculateBBox(node.getCoreDistance());
					node.calculateBBox();
					kdTree.queryNextNeighbor(kdTree.getRoot(), node, mrEdges, forest);
				}else if(node.getBboxDistance() < node.getIntervals().last()){
					node.calculateBBox();
					kdTree.queryNextNeighbor(kdTree.getRoot(), node, mrEdges, forest);
				}else if(!Double.isNaN(node.getBboxDistance())){
					ArrayListVisitor v = new ArrayListVisitor();
					kdTree.queryExcludeBBox(kdTree.getRoot(), v, node.getBbox());
					for(Object item : v.getItems()){
						KdNode neighbor = (KdNode) item;
						Double distance = node.computeDistance(node.getCoordinate(), neighbor.getCoordinate());
						mrEdges.add(new MutualReachabilityEdge(node, neighbor,distance));
					}
					node.setBboxDistance(Double.NaN);
				}
			}
			count += 1;
			
			Double spanningTreeCost = 0.0;
			for(MutualReachabilityEdge edge : mrEdges){
				KdNode source = edge.getNode1();
	            KdNode target = edge.getNode2();
	            if (forest.find(source).equals(forest.find(target))) {
	                continue;
	            }

	            forest.union(source, target);
	            DefaultWeightedEdge e = swg.addEdge(new ClusterNode(source), new ClusterNode(target));
	            swg.setEdgeWeight(e, edge.getMrDistance());
	            spanningTreeCost += edge.getMrDistance();
			}
			mrEdges.clear();
				
		}
		return swg;
	}
	
	
	public static void createKmstWKT(SimpleWeightedGraph<KdNode, DefaultWeightedEdge> kmst){
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(),4326);
		
		try{
			File file = new File("testWkt.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("v1,v2,weight,wkt");
			for(DefaultWeightedEdge e : kmst.edgeSet()){
				KdNode node1 = kmst.getEdgeSource(e);
				KdNode node2 = kmst.getEdgeTarget(e);
				Coordinate point1 =node1.getCoordinate();
				Coordinate point2 = node2.getCoordinate();
				Coordinate[] coords = {point1,point2};
				bw.write("\n\"" + node1.getLabel() + "\"" + "," + "\"" + node2.getLabel() + "\"" + "," +
						"\"" + kmst.getEdgeWeight(e) + "\"" + "," +"\"" + gf.createLineString(coords) + "\"");
			}
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void createClusterWKT(UndirectedWeightedSubgraph<ClusterNode, DefaultWeightedEdge> clusterGraph){
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(),4326);
		
		try{
			File file = new File("testClusterWkt.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("v1,v2,weight,wkt");
//			for(DefaultWeightedEdge e : clusterGraph.edgeSet()){
//				System.out.println(e);
//			}
			System.out.println("Create Cluster");
			int count = 0;
			for (DefaultWeightedEdge edge : clusterGraph.edgeSet()){
				count += 1;
				if (count > 10) break;
				System.out.println(edge);
			}
			for(DefaultWeightedEdge e : clusterGraph.edgeSet()){
				ClusterNode node1 = clusterGraph.getEdgeSource(e);
				ClusterNode node2 = clusterGraph.getEdgeTarget(e);
//				System.out.println(node1 + "\t" + node2 + "\t" + clusterGraph.getEdgeWeight(e));
				Coordinate point1 =node1.getCoord();
				Coordinate point2 = node2.getCoord();
				Coordinate[] coords = {point1,point2};
				bw.write("\n\"" + (node1.getCluster() != null ? node1.getCluster().getLabel() : "None") + "\"" + "," + "\"" + (node2.getCluster() != null ? node2.getCluster().getLabel() : "None") + "\"" + "," + "\"" + gf.createLineString(coords) + "\"");
			}
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads in the input data set from the file given, assuming the delimiter separates attributes
	 * for each data point, and each point is given on a separate line.  Error messages are printed
	 * if any part of the input is improperly formatted.
	 * @param fileName The path to the input file
	 * @param delimiter A regular expression that separates the attributes of each point
	 * @return A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @throws IOException If any errors occur opening or reading from the file
	 */
	public static Coordinate[] readInDataSet(String fileName, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<double[]> dataSet = new ArrayList<double[]>();
		int numAttributes = -1;
		int lineIndex = 0;
		String line = reader.readLine();

		while (line != null) {
			lineIndex++;
			String[] lineContents = line.split(delimiter);

			if (numAttributes == -1)
				numAttributes = lineContents.length;
			else if (lineContents.length != numAttributes)
				System.err.println("Line " + lineIndex + " of data set has incorrect number of attributes.");

			double[] attributes = new double[numAttributes];
			for (int i = 0; i < numAttributes; i++) {
				try {
					//If an exception occurs, the attribute will remain 0:
					attributes[i] = Double.parseDouble(lineContents[i]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			dataSet.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		Coordinate[] finalDataSet = new Coordinate[dataSet.size()];

		for (int i = 0; i < dataSet.size(); i++) {
			double[] point = dataSet.get(i);
			finalDataSet[i] = new Coordinate(point[0],point[1]);
		}
		return finalDataSet;
	}
	 public static void main(String[] args) {
		try{
			Coordinate[] data = readInDataSet("data/testData.csv", ",");
			long startTime = System.currentTimeMillis();
			NearestKdTree tree = calculateNearestKdTree(data, 3, 0.001);
			System.out.println("Time to calculate NN: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			SimpleWeightedGraph<ClusterNode, DefaultWeightedEdge> kmst = calculateMST(tree);
			System.out.println("Time add edges to create Minimum Spanning Tree: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			Double maxWeight = 0.0;
			for(DefaultWeightedEdge e : kmst.edgeSet()){
				Double currWeight = kmst.getEdgeWeight(e);
				if(currWeight > maxWeight){
					maxWeight = currWeight;
				}
			}
			Cluster rootCluster = new Cluster(null,maxWeight,3,new UndirectedWeightedSubgraph<>(kmst, null, null));
			ClusterHeirarchy ch = new ClusterHeirarchy(rootCluster);
			System.out.println("Build root cluster and init heirarchy: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			ch.makeHeirarchy();
			System.out.println("Make Heirarchy:" + (System.currentTimeMillis() - startTime));
			int count = 0;
			for(ClusterNode node : ch.getRoot().getGraph().vertexSet()){
				count += 1;
				if (count > 10) break;
				System.out.println(node);
			}
//			for(DefaultWeightedEdge e : rootCluster.getGraph().edgeSet()){
//				System.out.println("Root:\n");
//				System.out.println(rootCluster.getGraph().getEdgeSource(e));
//				System.out.println(rootCluster.getGraph().getEdgeTarget(e));
//				System.out.println(rootCluster.getGraph().getEdgeWeight(e));
//				System.out.println("KMST:\n");
//				System.out.println(kmst.getEdgeSource(e));
//				System.out.println(kmst.getEdgeTarget(e));
//				System.out.println(kmst.getEdgeWeight(e));
//			}
			createClusterWKT(ch.getRoot().getGraph());
			System.out.println("Write MST to WKT: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			

		}catch(IOException e){
			System.out.println(e);
		}
	}
}
