package vista.graph;

/**
 * Represents the state of selection of a 2D area
 * 
 * @author psandhu
 * 
 */
public interface RangeSelected {
	/**
	 * gets the minimum of range on x axis
	 */
	public double getXRangeMin();

	/**
	 * gets the maximum of range on x axis
	 */
	public double getXRangeMax();

	/**
	 * gets the minimum of range on y axis
	 */
	public double getYRangeMin();

	/**
	 * gets the maximum of range on y axis
	 */
	public double getYRangeMax();
}
