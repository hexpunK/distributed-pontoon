package distributedpontoon.shared;

import java.io.Serializable;

/**
 * Stores a pair of objects in a related group. Accepts any {@link Object} child
 *  class, with both the left and right components capable of holding different 
 * types.
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
    protected L left;
    /** The right value for this {@link Pair}. */
    protected R right;
    
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }
    
    public void setLeft(L newLeft) { this.left = newLeft; }
    
    public L getLeft() { return this.left; }
    
    public void setRight(R newRight) { this.right = newRight; }
    
    public R getRight() { return this.right; }

    @Override
    public int hashCode() 
    {
        return (left.hashCode() ^ right.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof Pair)) return false;
        Pair other = (Pair)obj;
        return this.left.equals(other.left) && this.right.equals(other.right);
    }
}
