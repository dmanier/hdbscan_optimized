package hdbscan;

import com.vividsolutions.jts.geom.Coordinate;

public class ClusterNode {
	
	private Coordinate coord;
	private Integer cluster;
	private Double deathLevel;
	private Double outlierScore;
	
	public ClusterNode(KdNode kdNode){
		this.coord = kdNode.getCoordinate();
		this.cluster = null;
		this.deathLevel = null;
		this.outlierScore = null;
	}
	
	public ClusterNode(KdNode kdNode, Cluster cluster){
		this.coord = kdNode.getCoordinate();
		this.cluster = cluster.getLabel();
		this.deathLevel = null;
		this.outlierScore = null;
	}

	public Integer getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster.getLabel();
	}

	public Double getDeathLevel() {
		return deathLevel;
	}

	public void setDeathLevel(Double deathLevel) {
		this.deathLevel = deathLevel;
	}

	public Double getOutlierScore() {
		return outlierScore;
	}

	public void setOutlierScore(Double outlierScore) {
		this.outlierScore = outlierScore;
	}

	public Coordinate getCoord() {
		return coord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coord == null) ? 0 : coord.hashCode());
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
		ClusterNode other = (ClusterNode) obj;
		if (coord == null) {
			if (other.coord != null)
				return false;
		} else if (!coord.equals(other.coord))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClusterNode [coord=" + coord + ", cluster=" + ((cluster != null)?cluster : "None") + ", deathLevel=" + deathLevel + ", outlierScore="
				+ outlierScore + "]";
	}
	

}
