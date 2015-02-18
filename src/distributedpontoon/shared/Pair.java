package distributedpontoon.shared;

import java.io.Serializable;

/**
 * Stores a pair of objects in a related group. Accepts any {@link Object} child
 *  class, with both the left and right components capable of holding different 
 * types. A {@link Pair} is immutable once constructed.
 * 
 * Why isn't something this simple in the Java Standard Library...
 * 
 * @param <L> The type for the left component in this {@link Pair}.
 * @param <R> The type for the right component in this {@link Pair}.
 * @author 6266215
 * @version 1.0
 * @since 2015-02-10
 */
public class Pair<L, R> implements Serializable
{
    /** The left value for this {@link Pair}. */
    public final L Left;
    /** The right value for this {@link Pair}. */
    public final R Right;
    
    /**
     * Creates a new {@link Pair} with the specified left and right items.
     * 
     * @param left The object to store in the left value of this {@link Pair}.
     * @param right The object to store in the right value of this {@link Pair}.
     * @since 1.0
     */
    public Pair(L left, R right)
    {
        this.Left = left;
        this.Right = right;
    }

    /**
     * Overrides the default {@link Object#hashCode()} method to ensure that the
     *  hash for two {@link Pair}s with the same contents will match.
     * 
     * @return 
     * @see Object#hashCode() 
     * @since 1.0
     */
    @Override
    public int hashCode() 
    {
        return (Left.hashCode() ^ Right.hashCode());
    }

    /**
     * Compares this {@link Pair} against an {@link Object} to see if they 
     * contain the same data. If the specified object for comparison cannot be 
     * cast to {@link Pair}, or is null, the comparison will fail.
     * 
     * @param obj The {@link Object} to compare against.
     * @return Returns true if this {@link Pair} and the specified object 
     * contain the same data. Returns false otherwise.
     * @see Object#equals(java.lang.Object) 
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof Pair)) return false;
        Pair other = (Pair)obj;
        return this.Left.equals(other.Left) && this.Right.equals(other.Right);
    }

    /**
     * Returns information about the contents of this {@link Pair}. The left 
     * and right objects should have their own {@link Object#toString()} 
     * overrides for this to return sensible information.
     * 
     * @return A {@link String} containing the details of this {@link Pair}.
     * @since 1.0
     */
    @Override
    public String toString()
    {
        return String.format("%s - %s", Left.toString(), Right.toString());
    }
}
