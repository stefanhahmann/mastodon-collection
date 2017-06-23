package org.mastodon.bufferpooldemo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.ObjPropertyMap;

import net.imglib2.RealPoint;

public class Vector3Example
{
	public static void main( final String[] args )
	{
		final Vector3Pool pool = new Vector3Pool( 11 );

		// Object vs PoolObject
		{
			final RealPoint p = new RealPoint( 1.0, 1.0, 1.0 );
			final Vector3 v = pool.create().init( 1.0, 1.0, 1.0 );
			System.out.println( p );
			System.out.println( v );
		}


		// ArrayList stores one proxy Object for each element
		{
			final List< Vector3 > vecs = new ArrayList<>();
			for ( int i = 0; i < 10; ++i )
				vecs.add( pool.create().init( i, i, i ) );
			System.out.println( vecs );
		}

		// Reusing proxies.
		// RefArrayList just stores pool indices in Trove TIntArrayList. No proxy Objects.
		{
			final Vector3 ref = pool.createRef();

			final List< Vector3 > vecs = new RefArrayList<>( pool );
			for ( int i = 0; i < 3; ++i )
				vecs.add( pool.create( ref ).init( i, i, i ) );
			System.out.println( vecs );

			pool.releaseRef( ref );
		}

		// Be careful when reusing proxies.
		{
			final Vector3 ref = pool.createRef();

			@SuppressWarnings( "unused" )
			final Vector3 v2 = pool.create( ref ).init( 2, 2, 2 );
			@SuppressWarnings( "unused" )
			final Vector3 v3 = pool.create( ref ).init( 3, 3, 3 ); // v3 == v2 == ref !!!

			pool.releaseRef( ref );
		}

		// PropertyMaps
		{
			final ObjPropertyMap< Vector3, String > color = new ObjPropertyMap<>( pool );
			final DoublePropertyMap< Vector3 > radius = new DoublePropertyMap<>( pool, Double.NEGATIVE_INFINITY );
			final Vector3 ref = pool.createRef();

			final List< Vector3 > vecs = new RefArrayList< >( pool );
			for ( int i = 0; i < 10; ++i )
			{
				final Vector3 v = pool.create( ref ).init( i, i, i );
				if ( i % 2 == 0 )
					color.set( v, "blue" );
				vecs.add( v );
			}

			vecs.stream()
				.filter( v -> color.get( v ) == "blue" )
				.forEach( v -> System.out.println( "LOOK!!! a blue vector! " + v ) );

			final Vector3 v = vecs.get( 5 );
			color.get( v );
			radius.getDouble( v );

			pool.releaseRef( ref );
		}

		// FloatBuffer currently (!) underlying the pool
		final FloatBuffer floatBuffer = pool.getFloatBuffer();
		final float[] dataForPrinting = new float[ pool.size() * pool.layout.getSizeInBytes() / 4 ];
		floatBuffer.rewind();
		floatBuffer.get( dataForPrinting );
		for ( int i = 0; i < dataForPrinting.length; ++i )
		{
			System.out.print( dataForPrinting[ i ] );
			System.out.print( "  " );
			if ( i % 10 == 9 )
				System.out.println();
		}
	}
}
