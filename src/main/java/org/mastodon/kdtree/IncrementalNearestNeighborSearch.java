/*-
 * #%L
 * Mastodon Collections
 * %%
 * Copyright (C) 2015 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.kdtree;

import net.imglib2.RealCursor;
import net.imglib2.RealLocalizable;

/**
 * Incremental nearest-neighbor search in set of points. The interface describes
 * implementations that perform the search for a specified location and provide
 * iteration of points in order of increasing distance to the query location.
 * Iteration is implemented through the {@link RealCursor} interface, providing
 * access to the data, location and distance to the current nearest neighbor
 * until the iterator is forwarded or the next search is performed.
 * 
 * @param <T>
 *            the type of point.
 * 
 * @author Tobias Pietzsch
 */
public interface IncrementalNearestNeighborSearch< T > extends RealCursor< T >
{
	/**
	 * Perform nearest-neighbor search for a reference coordinate.
	 *
	 * @param reference
	 *            the coordinate to search for.
	 */
	public void search( final RealLocalizable reference );

	/**
	 * Access the square Euclidean distance between the reference location as
	 * used for the last search and the current nearest neighbor.
	 * 
	 * @return the square distance.
	 */
	public double getSquareDistance();

	/**
	 * Access the Euclidean distance between the reference location as used for
	 * the last search and the current nearest neighbor.
	 * 
	 * @return the distance.
	 */
	public double getDistance();

	/**
	 * Create a copy.
	 */
	@Override
	public IncrementalNearestNeighborSearch< T > copy();
}
