package net.trackmate.revised.model.mamut;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.spatial.SpatialIndex;

public class BoundingSphereRadiusStatistics implements GraphListener< Spot, Link >
{
	/**
	 * Int value used to declare that the requested timepoint is not in a map.
	 * Timepoints are always >= 0, so -1 works...
	 */
	private final static int NO_ENTRY_KEY = -1;

	private final Model model;

	private final ListenableGraph< Spot, Link > graph;

	private final TIntObjectHashMap< Stats > timepointToStats;

	private final Lock readLock;

    private final Lock writeLock;

	public BoundingSphereRadiusStatistics( final Model model )
	{
		this.model = model;
		this.graph = model.getGraph();
		timepointToStats = new TIntObjectHashMap< Stats >( 10, 0.5f, NO_ENTRY_KEY );
		graph.addGraphListener( this );
		final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	    readLock = rwl.readLock();
	    writeLock = rwl.writeLock();
		init();
	}

	public Lock readLock()
	{
		return readLock;
	}

	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		final Stats stats = timepointToStats.get( timepoint );
		if ( stats == null )
			return -1;
		else
			return stats.getMaxRadiusSquared();
	}

	private void init()
	{
	    timepointToStats.clear();
	    for ( final Spot v : graph.vertices() )
	    {
	    	final int t = v.getTimepoint();
	    	Stats stats = timepointToStats.get( t );
	    	if ( stats == null )
	    	{
	    		stats = new Stats( graph.vertexRef() );
	    		timepointToStats.put( t, stats );
	    	}
	    	stats.add( v );
	    }
	}

	@Override
	public void vertexAdded( final Spot v )
	{
		writeLock.lock();
		try
		{
	    	final int t = v.getTimepoint();
	    	Stats stats = timepointToStats.get( t );
	    	if ( stats == null )
	    	{
	    		stats = new Stats( graph.vertexRef() );
	    		timepointToStats.put( t, stats );
	    	}
	    	stats.add( v );
		}
		finally
		{
			writeLock.lock();
		}
	}

	@Override
	public void vertexRemoved( final Spot v )
	{
		writeLock.lock();
		try
		{
			final int t = v.getTimepoint();
	    	final SpatialIndex< Spot > spatialIndex = model.getSpatioTemporalIndex().getSpatialIndex( t );
	    	if ( spatialIndex.isEmpty() )
	    		timepointToStats.remove( t );
	    	else
	    	{
		    	final Stats stats = timepointToStats.get( t );
	    		stats.remove( v, model.getSpatioTemporalIndex().getSpatialIndex( t ) );
	    	}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void edgeAdded( final Link edge )
	{}

	@Override
	public void edgeRemoved( final Link edge )
	{}

	@Override
	public void graphRebuilt()
	{
		writeLock.lock();
		try
		{
			init();
		}
		finally
		{
			writeLock.unlock();
		}
	}

//	@Override // TODO: should be implemented for some listener interface
	public void vertexAttributeChanged( final Spot v )
	{
		writeLock.lock();
		try
		{
			final int t = v.getTimepoint();
	    	final Stats stats = timepointToStats.get( t );
	    	stats.radiusChanged( v, model.getSpatioTemporalIndex().getSpatialIndex( t ) );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	static class Stats
	{
		private double maxRadiusSquared;

		private final Spot spotWithMaxRadiusSquared;

		public Stats( final Spot ref )
		{
			this.spotWithMaxRadiusSquared = ref;
			maxRadiusSquared = 0;
		}

		public void add( final Spot spot )
		{
			final double r2 = spot.getBoundingSphereRadiusSquared();
			if ( r2 > maxRadiusSquared )
			{
				maxRadiusSquared = r2;
				spotWithMaxRadiusSquared.refTo( spot );
			}
		}

		/**
		 * If {@code spot} is the representative for the max radius, we have to
		 * find the new max radius from all spots. Otherwise, we're lucky and
		 * we're done. Assumes that {@code spots} does not contain {@code spot}.
		 */
		public void remove( final Spot spot, final Iterable< Spot > spots )
		{
			if ( spotWithMaxRadiusSquared.equals( spot ) )
			{
				maxRadiusSquared = 0;
				for ( final Spot v : spots )
					add( v );
			}
		}

		/**
		 * If {@code spot} is the representative for the max radius and its
		 * radius decreased, we have to find the new max radius from all spots.
		 */
		public void radiusChanged( final Spot spot, final Iterable< Spot > spots )
		{
			final double r2 = spot.getBoundingSphereRadiusSquared();
			if ( r2 > maxRadiusSquared )
			{
				maxRadiusSquared = r2;
				spotWithMaxRadiusSquared.refTo( spot );
			}
			else if ( spotWithMaxRadiusSquared.equals( spot ) && r2 < maxRadiusSquared )
			{
				// this does the right thing though it doesn't sound like it
				remove( spot, spots );
			}
		}

		public double getMaxRadiusSquared()
		{
			return maxRadiusSquared;
		}
	}
}
