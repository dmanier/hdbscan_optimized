package hdbscan;


public class MutualReachabilityEdge implements Comparable<MutualReachabilityEdge>{
	
	private int label1;
	private KdNode node1;
	private int label2;
	private KdNode node2;
	private Double mrDistance;
	
	public MutualReachabilityEdge(KdNode node1, KdNode node2){
		if(node1.getCoreDistance() >= node2.getCoreDistance()){
			this.label1 = node1.getLabel();
			this.node1 = node1;
			this.label2 = node2.getLabel();
			this.node2 = node2;
			this.mrDistance = node1.getCoreDistance();
		}else{
			this.label1 = node2.getLabel();
			this.node1 = node2;
			this.label2 = node1.getLabel();
			this.node2 = node1;
			this.mrDistance = node2.getCoreDistance();
		}
	}
	
	public MutualReachabilityEdge(KdNode node1, KdNode node2, Double distance){
		if(node1.getCoreDistance() >= node2.getCoreDistance()){
			this.label1 = node1.getLabel();
			this.node1 = node1;
			this.label2 = node2.getLabel();
			this.node2 = node2;
			this.mrDistance = node1.getCoreDistance();
		}else{
			this.label1 = node2.getLabel();
			this.node1 = node2;
			this.label2 = node1.getLabel();
			this.node2 = node1;
			this.mrDistance = node2.getCoreDistance();
		}
		if(this.mrDistance < distance) this.mrDistance = distance;
	}
	

	public KdNode getNode1() {
		return node1;
	}


	public KdNode getNode2() {
		return node2;
	}


	public int getLabel1() {
		return label1;
	}


	public int getLabel2() {
		return label2;
	}


	public Double getMrDistance() {
		return mrDistance;
	}


	@Override
	public String toString() {
		return "MutualReachabilityEdge [label1=" + label1 + ", label2=" + label2 + ", mrDistance=" + mrDistance + "]";
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + label1;
		result = prime * result + label2;
		long temp;
		temp = Double.doubleToLongBits(mrDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		MutualReachabilityEdge other = (MutualReachabilityEdge) obj;
		if (label1 != other.label1)
			return false;
		if (label2 != other.label2)
			return false;
		if (Double.doubleToLongBits(mrDistance) != Double.doubleToLongBits(other.mrDistance))
			return false;
		return true;
	}

	@Override
	public int compareTo(MutualReachabilityEdge e2) {
		// TODO Auto-generated method stub
		return (this.mrDistance > e2.mrDistance ? 1 : (this.mrDistance < e2.mrDistance ? -1 : 
			(this.label1 > e2.label1 ? 1 : (this.label1 < e2.label1 ? -1 : 
				(this.label2 > e2.label2 ? 1 : (this.label2 < e2.label2 ? -1 : 0))))));
	}
}
