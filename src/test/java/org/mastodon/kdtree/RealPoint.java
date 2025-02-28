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

import org.mastodon.util.DelegateRealLocalizable;
import org.mastodon.util.DelegateRealPositionable;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.attributes.RealPointAttributeValue;

import net.imglib2.RealLocalizable;

class RealPoint extends PoolObject< RealPoint, RealPointPool, ByteMappedElement >
		implements DelegateRealLocalizable, DelegateRealPositionable
{
	private final RealPointAttributeValue position;

	RealPoint( final RealPointPool pool )
	{
		super( pool );
		position = pool.position.createQuietAttributeValue( this );
//		position = pool.position.createAttributeValue( this );
	}

	public RealPoint init( final double... position )
	{
		pool.position.setPositionQuiet( this, position );
		return this;
	}

	public RealPoint init( final RealLocalizable position )
	{
		pool.position.setPositionQuiet( this, position );
		return this;
	}

	@Override
	public String toString()
	{
		final int n = numDimensions();
		final StringBuilder sb = new StringBuilder();
		sb.append( "( " );
		for ( int d = 0; d < n; d++ )
		{
			sb.append( getDoublePosition( d ) );
			if ( d < n - 1 )
				sb.append( ", " );
		}
		sb.append( " )" );
		return sb.toString();
	}

	@Override
	protected void setToUninitializedState()
	{}

	@Override
	public RealPointAttributeValue delegate()
	{
		return position;
	}
}
