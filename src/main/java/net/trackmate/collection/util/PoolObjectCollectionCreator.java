package net.trackmate.collection.util;

import java.util.Collection;
import java.util.Iterator;

import net.trackmate.pool.Pool;
import net.trackmate.pool.PoolObject;

/**
 * Wrapper for {@link Pool}s offering the ability to create collections based on
 * the wrapped pool.
 * <p>
 * This class wraps a {@link Pool} and offers methods to generate various
 * collections based on the wrapped pool. It offers a bridge between the
 * {@link Pool} framework and the Java {@link Collection} framework.
 * <p>
 * This class also implements the {@link Collection} interface itself, and
 * therefore allows for questing the underlying pool using the
 * {@link Collection} methods. However some methods that are unsuited for pools
 * throw an {@link UnsupportedOperationException}:
 * <ul>
 * <li>{@link #contains(Object)}
 * <li>{@link #containsAll(Collection)}
 * <li>{@link #toArray()}
 * <li>{@link #toArray(Object[])}
 * <li>{@link #add(Object)}
 * <li>{@link #addAll(Collection)}
 * <li>{@link #remove(Object)}
 * <li>{@link #removeAll(Collection)}
 * <li>{@link #retainAll(Collection)}
 * <li>{@link #clear()}
 * </ul>
 * If these methods are needed, it is probably best to create an adequate
 * collection from the pool using the <i>create*</i> methods.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link Pool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class PoolObjectCollectionCreator< O extends PoolObject< O, ? > > extends AbstractRefPoolCollectionCreator< O, Pool< O, ? > >
{
	public PoolObjectCollectionCreator( final Pool< O, ? > pool )
	{
		super( pool );
	}

	@Override
	public int size()
	{
		return pool.size();
	}

	@Override
	public Iterator< O > iterator()
	{
		return pool.iterator();
	}
}
