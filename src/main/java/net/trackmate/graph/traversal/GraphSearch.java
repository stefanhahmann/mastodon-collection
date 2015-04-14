package net.trackmate.graph.traversal;

import java.util.Comparator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.AbstractGraphAlgorithm;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;

public abstract class GraphSearch< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{

	protected final RefSet< V > discovered;

	protected final RefSet< V > processed;

	protected int time;

	private boolean aborted;

	protected SearchListener< V, E > searchListener;

	protected final RefRefMap< V, V > parents;

	protected Comparator< V > comparator;

	public GraphSearch(final Graph< V, E > graph)
	{
		super( graph );
		this.discovered = createVertexSet();
		this.processed = createVertexSet();
		this.parents = createVertexVertexMap();
	}

	/**
	 * Starts the search at the specified vertex.
	 * <p>
	 * This method returns when the search is complete, or when the
	 * {@link SearchListener} aborts the search by calling the {@link #abort()}
	 * method on this search.
	 * 
	 * @param vertex
	 *            the vertex to start the search with.
	 */
	public void start( final V start )
	{
		discovered.clear();
		processed.clear();
		parents.clear();
		time = 0;
		aborted = false;
		visit( start );
	}

	/**
	 * Sets the {@link SearchListener} to use for next search.
	 * <p>
	 * If it is not <code>null</code>, this listener will be notified in proper
	 * order when discovering vertices, crossing edges and finishing processing
	 * vertices. If <code>null</code>, there are no notifications.
	 * 
	 * @param searchListener
	 *            the search listener to use for next search. Can be
	 *            <code>null</code>.
	 */
	public void setTraversalListener( final SearchListener< V, E > searchListener )
	{
		this.searchListener = searchListener;
	}

	/**
	 * Sets the comparator to use for next search.
	 * <p>
	 * This comparator is used when several children of the current vertex can
	 * be visited. If the specified comparator is not <code>null</code>, it is
	 * used to sort these children, which are then visited according to the
	 * order it sets. If it is <code>null</code>, the order is unspecified.
	 * 
	 * @param comparator
	 *            the vertex comparator to use for next search. Can be
	 *            <code>null</code>.
	 */
	public void setComparator( final Comparator< V > comparator )
	{
		this.comparator = comparator;
	}

	/**
	 * Aborts the current search before its normal termination.
	 */
	public void abort()
	{
		aborted = true;
	}

	/**
	 * Returns <code>true</code> if the search was aborted before its normal
	 * completion.
	 * 
	 * @return <code>true</code> if the search was aborted.
	 */
	public boolean wasAborted()
	{
		return aborted;
	}

	/**
	 * Returns the parent of the specified vertex in the current search tree.
	 * Returns <code>null</code> if the specified vertex has not been visited
	 * yet.
	 * 
	 * @param child
	 *            the vertex to find the parent of.
	 * @return the vertex parent in the search tree.
	 */
	public V parent( final V child )
	{
		return parents.get( child );
	}

	/**
	 * Returns the time of visit for the specified vertex. Actual meaning depend
	 * on the concrete search implementation.
	 * 
	 * @param vertex
	 *            the vertex to time.
	 * @return the vertex discovery time.
	 */
	public int timeOf( final V vertex )
	{
		return -1; // TODO :( how can I store this elegantly?
	}

	/**
	 * Computes the specified edge class in the current search. Return
	 * {@link EdgeClass#UNCLASSIFIED} if the edge has not been visited yet.
	 * 
	 * @param from
	 *            the vertex visited first while crossing the edge.
	 * @param to
	 *            the vertex visited last while crossing the edge.
	 * @return the edge class.
	 */
	public EdgeClass edgeClass( final V from, final V to )
	{
		if ( from.equals( parents.get( to ) ) ) { return EdgeClass.TREE; }
		if ( discovered.contains( to ) && !processed.contains( to ) ) { return EdgeClass.BACK; }
		if ( processed.contains( to ) )
		{
			if ( timeOf( from ) < timeOf( to ) )
			{
				return EdgeClass.FORWARD;
			}
			else
			{
				return EdgeClass.CROSS;
			}
		}
		return EdgeClass.UNCLASSIFIED;
	}

	/**
	 * Enumeration of the possible edge class during a graph search.
	 * 
	 * @author Jean-Yves Tinevez
	 */
	public static enum EdgeClass
	{
		TREE,
		BACK,
		FORWARD,
		CROSS,
		UNCLASSIFIED;
	}

	protected abstract void visit( V vertex );

}
