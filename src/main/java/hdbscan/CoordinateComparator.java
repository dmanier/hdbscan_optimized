package hdbscan;
import java.util.Comparator;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Coordinate ;

/**
 * Defines an ordering for {@link Coordinate}s which depends exclusively on the 
 * value of the specified axis. Instances of this class are created by the 
 * {@link #getComparator(int)} factory method. Axes are numbered as follows: 
 * 
 * <ul>
 * <li> 0 == x </li>
 * <li> 1 == y </li>
 * <li> 2 == z </li>
 * </ul>
 * 
 * <p>Other axis values are invalid.</p>
 * 
 * <p>This is used primarily during the construction of a balanced KdTree from
 * a list of points, but can be used wherever it is necessary to sort coordinates
 * by a single axis value.</p>
 * 
 * @author Bryce Nordgren
 * @since 1.12
 *
 */
public class CoordinateComparator implements Comparator {
	
	private static CoordinateComparator []instances = 
		new CoordinateComparator[3] ;
	
	static { 
		instances[0] = null ;
		instances[1] = null ; 
		instances[2] = null ; 
	}
	
	private int axis = 0 ; 
	
	private CoordinateComparator(int _axis) { 
		axis = _axis ; 
	}

	public int compare(Object i1, Object i2) {
		double v1, v2 ; 
		
		if (!(i1 instanceof Coordinate) || !(i2 instanceof Coordinate)) { 
			return -1 ; 
		}
		
		Coordinate o1 = (Coordinate)i1 ; 
		Coordinate o2 = (Coordinate)i2 ; 
		switch (axis) { 
		case 0 : 
			v1 = o1.x ; 
			v2 = o2.x ; 
			break ; 
		case 1 : 
			v1 = o1.y ; 
			v2 = o2.y ; 
			break ; 
		case 2 : 
			v1 = o1.z ; 
			v2 = o2.z ; 
			break ; 
		default : 
			v1 = v2 = Double.NaN ; 
		}
		
		if (v1 < v2) { 
			return -1 ;
		} else if (v1 > v2) { 
			return 1 ;
		} else {
			return 0 ; 
		}
	}
	
	/**
	 * Returns a comparator for the specified axis. 
	 * @param axis the axis to sort by (see class javadoc)
	 * @return the desired comparator, or null if the axis is out of range.
	 */
	public static CoordinateComparator getComparator(int axis) {
		if ((axis <0) || (axis >=3)) return null ;
		if (instances[axis] == null) {
			instances[axis] = new CoordinateComparator(axis) ; 
		}
		return instances[axis] ; 
	}
}
