package distributedpontoon.shared;

import java.io.Serializable;

/**
 * Stores three objects in a related group. Accepts any {@link Object} child
 *  class, with each of the components capable of holding different types. A 
 * {@link Triple} is immutable once constructed.
 * 
 * Why isn't something this simple in the Java Standard Library...
 * 
 * @param <L> The type for the first component in this {@link Triple}.
 * @param <M> The type for the second component in this {@link Triple}.
 * @param <R> The type for the third component in this {@link Triple}.
 * @author 6266215
 * @version 1.0
 * @since 2015-02-10
 */
public class Triple<L, M, R> implements Serializable
{
    /** The first value for this {@link Triple}. */
    public final L One;
    /** The second value for this {@link Triple}. */
    public final M Two;
    /** The third value for this {@link Triple}. */
    public final R Three;
    
    /**
     * Creates a new {@link Triple} with the specified objects.
     * 
     * @param one The object in the first value of this {@link Triple}.
     * @param two The object in the second value of this {@link Triple}.
     * @param three The object in the third value of this {@link Triple}.
     * @since 1.0
     */
    public Triple(L one, M two, R three)
    {
        this.One = one;
        this.Two = two;
        this.Three = three;
    }

    /**
     * Overrides the default {@link Object#hashCode()} method to ensure that the
     *  hash for two {@link Triple}s with the same contents will match.
     * 
     * @return Returns a unique hash code as an int.
     * @see Object#hashCode() 
     * @since 1.0
     */
    @Override
    public int hashCode() 
    {
        return (One.hashCode() ^ Two.hashCode() ^ Three.hashCode());
    }

    /**
     * Compares this {@link Triple} against an {@link Object} to see if they 
     * contain the same data. If the specified object for comparison cannot be 
     * cast to {@link Triple}, or is null, the comparison will fail.
     * 
     * @param obj The {@link Object} to compare against.
     * @return Returns true if this {@link Triple} and the specified object 
     * contain the same data. Returns false otherwise.
     * @see Object#equals(java.lang.Object) 
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof Triple)) return false;
        Triple other = (Triple)obj;
        return this.One.equals(other.One) 
                && this.Two.equals(other.Two)
                && this.Three.equals(other.Three);
    }

    /**
     * Returns information about the contents of this {@link Triple}. The items 
     * should have their own {@link Object#toString()} overrides for this to 
     * return sensible information.
     * 
     * @return A {@link String} containing the details of this {@link Triple}.
     * @since 1.0
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        return String.format("%s - %s - %s", 
                One.toString(), Two.toString(), Three.toString());
    }
}
