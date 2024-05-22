package interf;

/**
 *
 * Represents a point in the path
 */
public interface IPoint {
    /**
     * Returns the X value of this point in the path
     * 
     * @return the X value of this point
     */
    int getX();

    /**
     * Returns the Y value of this point in the path
     * 
     * @return the Y value of this point
     */
    int getY();

    /**
     * Set the X value of this point in the path
     * 
     * 
     */
    void setX(int x);

    /**
     * Set the Y value of this point in the path
     * 
     * 
     */
    void setY(int y);

    /**
     * Returns a string representation of the Point
     * 
     * @return
     */
    String toString();
}
