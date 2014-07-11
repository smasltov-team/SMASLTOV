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
 * UndirectedEdge.java
 * -------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id: UndirectedEdge.java 1923 2007-11-13 14:17:48Z tla $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 10-Aug-2003 : General edge refactoring (BN);
 *
 */
package org._3pq.jgrapht.edge;

/**
 * A implementation for an undirected edge.
 *
 * @author Barak Naveh
 *
 * @since Jul 14, 2003
 */
public class UndirectedEdge extends DefaultEdge {
    /**
     * @see DefaultEdge#DefaultEdge(Object, Object)
     */
    public UndirectedEdge( Object sourceVertex, Object targetVertex ) {
        super( sourceVertex, targetVertex );
    }

    /**
     * Returns a string representation of this undirected edge. The
     * representation is a curly-braced pair {v1,v2} where v1,v2 are the two
     * endpoint vertices of this edge.
     *
     * @return a string representation of this directed edge.
     */
    public String toString(  ) {
        return "{" + getSource(  ) + "," + getTarget(  ) + "}";
    }
}
