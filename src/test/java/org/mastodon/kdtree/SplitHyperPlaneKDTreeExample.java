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

import java.util.Random;

import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.pool.DoubleMappedElement;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.position.transform.Round;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

public class SplitHyperPlaneKDTreeExample
{
	public static void main( final String[] args )
	{
		final int w = 800;
		final int h = 800;
		final int nPoints = 10000;

		// make random 2D Points
		final Random rand = new Random( 123124 );
		final RealPointPool pool = new RealPointPool( 2, nPoints );
		final RealPoint pRef = pool.createRef();
		final RefList< RealPoint > points = new RefArrayList<>( pool, nPoints );
		for ( int i = 0; i < nPoints; ++i )
		{
			final long x = rand.nextInt( w );
			final long y = rand.nextInt( h );
			points.add( pool.create( pRef ).init( x, y ) );
		}

		// split on hyperplane
		final HyperPlane plane = new HyperPlane( 1, 0.5, 600 );
		final KDTree< RealPoint, DoubleMappedElement > kdtree = KDTree.kdtree( points, pool );
		final SplitHyperPlaneKDTree< RealPoint, DoubleMappedElement > split = new SplitHyperPlaneKDTree<>( kdtree );
		split.split( plane );

		// show all points
		final Img< ARGBType > pointsImg = ArrayImgs.argbs( w, h );
		paint( points, pointsImg, new ARGBType( 0x00ff00 ) );
		ImageJFunctions.show( pointsImg );

		// show inside/outside points
		final Img< ARGBType > clipImg = ArrayImgs.argbs( w, h );
		paint( split.getAboveValues(), clipImg, new ARGBType( 0xffff00 ) );
		paint( split.getBelowValues(), clipImg, new ARGBType( 0x0000ff ) );
		ImageJFunctions.show( clipImg );
	}

	static void paint( final Iterable< ? extends RealLocalizable > points, final Img< ARGBType > output, final ARGBType color )
	{
		final int radius = 2;
		final RandomAccess< Neighborhood< ARGBType > > na = new HyperSphereShape( radius ).neighborhoodsRandomAccessible( Views.extendZero( output ) ).randomAccess();
		final Round< RandomAccess< Neighborhood< ARGBType > > > rna = new Round<>( na );
		for ( final RealLocalizable l : points )
		{
			rna.setPosition( l );
			for ( final ARGBType t : na.get() )
				t.set( color );
		}
	}
}
