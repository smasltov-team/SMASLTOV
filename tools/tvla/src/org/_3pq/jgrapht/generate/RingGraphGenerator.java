/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Lead:  Barak Naveh (barak_naveh@users.sourceforge.net)
 *
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------
 * GraphGenerator.java
 * -------------------
 * (C) Copyright 2003, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id: RingGraphGenerator.java 1923 2007-11-13 14:17:48Z tla $
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (JVS);
 *
 */
package org._3pq.jgrapht.generate;

import java.util.HashMap;
import java.util.Map;

import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.VertexFactory;

/**
 * RingGraphGenerator generates a ring graph of any
 * size. A ring graph is a graph that contains a single cycle that passes 
 * through all its vertices exactly once. For a directed graph, the generated 
 * edges are oriented consistently around the ring.
 *
 * @author John V. Sichi
 *
 * @since Sep 16, 2003
 */
public class RingGraphGenerator implements GraphGenerator {
    private int m_size;

    /**
     * Construct a new RingGraphGenerator.
     *
     * @param size number of vertices to be generated
     */
    public RingGraphGenerator( int size ) {
        if (size < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }
        m_size = size;
    }

    /**
     * @see GraphGenerator#generateGraph
     */
    public void generateGraph( Graph target, VertexFactory vertexFactory,
        Map resultMap ) {
        if( m_size < 1 ) {
            return;
        }

        LinearGraphGenerator linearGenerator =
            new LinearGraphGenerator( m_size );
        Map                  privateMap = new HashMap(  );
        linearGenerator.generateGraph( target, vertexFactory, privateMap );

        Object startVertex =
            privateMap.get( LinearGraphGenerator.START_VERTEX );
        Object endVertex = privateMap.get( LinearGraphGenerator.END_VERTEX );
        target.addEdge( endVertex, startVertex );
    }
}
