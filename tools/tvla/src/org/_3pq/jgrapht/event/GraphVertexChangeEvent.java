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
/* ---------------------------
 * GraphVertexChangeEvent.java
 * ---------------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id: GraphVertexChangeEvent.java 1923 2007-11-13 14:17:48Z tla $
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 *
 */
package org._3pq.jgrapht.event;

/**
 * An event which indicates that a graph vertex has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the
 * vertex has  been added or removed, or <i>before</i> it is added. The type
 * of the event can be tested using the {@link
 * org._3pq.jgrapht.event.GraphChangeEvent#getType()} method.
 *
 * @author Barak Naveh
 *
 * @since Aug 10, 2003
 */
public class GraphVertexChangeEvent extends GraphChangeEvent {
    /**
     * Before vertex added event. This event is fired before a vertex is added
     * to a graph.
     */
    public static final int BEFORE_VERTEX_ADDED = 11;

    /**
     * Before vertex removed event. This event is fired before a vertex is
     * removed from a graph.
     */
    public static final int BEFORE_VERTEX_REMOVED = 12;

    /**
     * Vertex added event. This event is fired after a vertex is added to a
     * graph.
     */
    public static final int VERTEX_ADDED = 13;

    /**
     * Vertex removed event. This event is fired after a vertex is removed from
     * a graph.
     */
    public static final int VERTEX_REMOVED = 14;

    /** The vertex that this event is related to. */
    protected Object m_vertex;

    /**
     * Creates a new GraphVertexChangeEvent object.
     *
     * @param eventSource the source of the event.
     * @param type the type of the event.
     * @param vertex the vertex that the event is related to.
     */
    public GraphVertexChangeEvent( Object eventSource, int type, Object vertex ) {
        super( eventSource, type );
        m_vertex = vertex;
    }

    /**
     * Returns the vertex that this event is related to.
     *
     * @return the vertex that this event is related to.
     */
    public Object getVertex(  ) {
        return m_vertex;
    }
}
