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
/* ------------------
 * DirectedGraph.java
 * ------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id: DirectedGraph.java 1923 2007-11-13 14:17:48Z tla $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 *
 */
package org._3pq.jgrapht;

import java.util.List;

/**
 * A graph whose all edges are directed. This is the root interface of all
 * directed graphs.
 * 
 * <p>
 * See <a href="http://mathworld.wolfram.com/DirectedGraph.html">
 * http://mathworld.wolfram.com/DirectedGraph.html</a> for more on directed
 * graphs.
 * </p>
 *
 * @author Barak Naveh
 *
 * @since Jul 14, 2003
 */
public interface DirectedGraph extends Graph {
    /**
     * Returns the "in degree" of the specified vertex. An in degree of a
     * vertex in a directed graph is the number of inward directed edges from
     * that vertex. See <a href="http://mathworld.wolfram.com/Indegree.html">
     * http://mathworld.wolfram.com/Indegree.html</a>.
     *
     * @param vertex vertex whose degree is to be calculated.
     *
     * @return the degree of the specified vertex.
     */
    public int inDegreeOf( Object vertex );


    /**
     * Returns a list of all edges incoming into the specified vertex.
     *
     * @param vertex the vertex for which the list of incoming edges to be
     *        returned.
     *
     * @return a list of all edges incoming into the specified vertex.
     */
    public List incomingEdgesOf( Object vertex );


    /**
     * Returns the "out degree" of the specified vertex. An out degree of a
     * vertex in a directed graph is the number of outward directed edges from
     * that vertex. See <a href="http://mathworld.wolfram.com/Outdegree.html">
     * http://mathworld.wolfram.com/Outdegree.html</a>.
     *
     * @param vertex vertex whose degree is to be calculated.
     *
     * @return the degree of the specified vertex.
     */
    public int outDegreeOf( Object vertex );


    /**
     * Returns a list of all edges outgoing from the specified vertex.
     *
     * @param vertex the vertex for which the list of outgoing edges to be
     *        returned.
     *
     * @return a list of all edges outgoing from the specified vertex.
     */
    public List outgoingEdgesOf( Object vertex );
}
