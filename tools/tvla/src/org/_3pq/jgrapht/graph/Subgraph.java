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
/* -------------
 * Subgraph.java
 * -------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id: Subgraph.java 1923 2007-11-13 14:17:48Z tla $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 26-Jul-2003 : Accurate constructors to avoid casting problems (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 23-Oct-2003 : Allowed non-listenable graph as base (BN);
 *
 */
package org._3pq.jgrapht.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.EdgeFactory;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.ListenableGraph;
import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.event.GraphEdgeChangeEvent;
import org._3pq.jgrapht.event.GraphListener;
import org._3pq.jgrapht.event.GraphVertexChangeEvent;

/**
 * A subgraph is a graph that has a subset of vertices and a subset of edges
 * with respect to some base graph. More formally, a subgraph G(V,E) that is
 * based on a base graph Gb(Vb,Eb) satisfies the following <b><i>subgraph
 * property</i></b>: V is a subset of Vb and E is a subset of Eb. Other than
 * this property, a subgraph is a graph with any respect and fully complies
 * with the <code>Graph</code> interface.
 * 
 * <p>
 * If the base graph is a {@link org._3pq.jgrapht.ListenableGraph}, the
 * subgraph becomes a "live-window" on the the base graph and guarantees the
 * subgraph property. If an edge or a vertex is removed from the base graph,
 * it is automatically removed from the subgraph. Subgraph listeners are
 * informed on such removal only if it results in a cascaded removal from the
 * subgraph. If edges or vertices are added to the base graph, the subgraph
 * remains unaffected.
 * </p>
 * 
 * <p>
 * If the base graph is <i>not</i> a ListenableGraph, then the subgraph
 * property cannot be guaranteed. If edges or vertices are removed from the
 * base graph, they are <i>not</i> removed from the subgraph.
 * </p>
 * 
 * <p>
 * Modifications to Subgraph are allowed as long as the subgraph property is
 * maintained. Addition of vertices or edges are allowed as long as they also
 * exist in the base graph. Removal of vertices or edges is always allowed.
 * The base graph is <i>never</i> affected by any modification made to the
 * subgraph.
 * </p>
 * 
 * <p>
 * PERFORMANCE NOTE: The intention of Subgraph is to provide a "window" on a
 * base graph, so that changes made to vertex instances or to edge instances
 * in the subgraph are immediately reflected in the base graph, and vice
 * versa. For that to happen, vertices and edges of the subgraph must be
 * reference-equal (and not only value-equal) to their respective ones in the
 * base graph. By default, this class verifies that this reference-equality is
 * maintained, but at a performance cost. You can avoid this cost by setting
 * the verifyIntegrity flag off. However, you must <i>exercise care</i> and
 * make sure that all vertices or edges you add to the subgraph are
 * reference-equal to the ones contained in the base graph.
 * </p>
 * 
 * <p>
 * At the time of writing, there is no easy and performance-friendly way to
 * programmatically ensure the reference-equality requirement: there is a
 * design flaw in the <code>java.util.Set</code> interface in that it provides
 * no way of obtaining a reference to an object already contained in a Set. If
 * fixed in the future the verifyIntegrity flag could be eliminated.
 * </p>
 *
 * @author Barak Naveh
 *
 * @see org._3pq.jgrapht.Graph
 * @see java.util.Set
 * @since Jul 18, 2003
 */
public class Subgraph extends AbstractGraph {
    private static final String REF_NOT_EQUAL_TO_BASE =
        "value-equal but not reference equal to base graph";
    private static final String NO_SUCH_EDGE_IN_BASE =
        "no such edge in base graph";
    private static final String NO_SUCH_VERTEX_IN_BASE =
        "no such vertex in base graph";

    //
    Set m_edgeSet   = new HashSet(  ); // friendly to improve performance
    Set m_vertexSet = new HashSet(  ); // friendly to improve performance

    // 
    private transient Set m_unmodifiableEdgeSet   = null;
    private transient Set m_unmodifiableVertexSet = null;
    private Graph         m_base;
    private boolean       m_verifyIntegrity       = true;

    /**
     * Creates a new Subgraph.
     *
     * @param base the base (backing) graph on which the subgraph will be
     *        based.
     * @param vertexSubset vertices to include in the subgraph. If
     *        <code>null</code> then all vertices are included.
     * @param edgeSubset edges to in include in the subgraph. If
     *        <code>null</code> then all the edges whose vertices found in the
     *        graph are included.
     */
    public Subgraph( Graph base, Set vertexSubset, Set edgeSubset ) {
        super(  );

        m_base = base;

        if( m_base instanceof ListenableGraph ) {
            ( (ListenableGraph) m_base ).addGraphListener( new BaseGraphListener(  ) );
        }

        addVerticesUsingFilter( base.vertexSet(  ), vertexSubset );
        addEdgesUsingFilter( base.edgeSet(  ), edgeSubset );
    }

    /**
     * @see org._3pq.jgrapht.Graph#getAllEdges(Object, Object)
     */
    public List getAllEdges( Object sourceVertex, Object targetVertex ) {
        List edges = null;

        if( containsVertex( sourceVertex ) && containsVertex( targetVertex ) ) {
            edges = new ArrayList(  );

            List baseEdges = m_base.getAllEdges( sourceVertex, targetVertex );

            for( Iterator iter = baseEdges.iterator(  ); iter.hasNext(  ); ) {
                Edge e = (Edge) iter.next(  );

                if( m_edgeSet.contains( e ) ) { // add if subgraph also contains it
                    edges.add( e );
                }
            }
        }

        return edges;
    }


    /**
     * @see org._3pq.jgrapht.Graph#getEdge(Object, Object)
     */
    public Edge getEdge( Object sourceVertex, Object targetVertex ) {
        List edges = getAllEdges( sourceVertex, targetVertex );

        if( edges == null || edges.isEmpty(  ) ) {
            return null;
        }
        else {
            return (Edge) edges.get( 0 );
        }
    }


    /**
     * @see org._3pq.jgrapht.Graph#getEdgeFactory()
     */
    public EdgeFactory getEdgeFactory(  ) {
        return m_base.getEdgeFactory(  );
    }


    /**
     * Sets the the check integrity flag.
     * 
     * <p>
     * WARNING: See discussion in the class description.
     * </p>
     *
     * @param verifyIntegrity
     *
     * @see Subgraph
     */
    public void setVerifyIntegrity( boolean verifyIntegrity ) {
        m_verifyIntegrity = verifyIntegrity;
    }


    /**
     * Returns the value of the verifyIntegrity flag.
     *
     * @return the value of the verifyIntegrity flag.
     */
    public boolean isVerifyIntegrity(  ) {
        return m_verifyIntegrity;
    }


    /**
     * @see org._3pq.jgrapht.Graph#addEdge(Object, Object)
     */
    public Edge addEdge( Object sourceVertex, Object targetVertex ) {
        assertVertexExist( sourceVertex );
        assertVertexExist( targetVertex );

        if( !m_base.containsEdge( sourceVertex, targetVertex ) ) {
            throw new IllegalArgumentException( NO_SUCH_EDGE_IN_BASE );
        }

        List edges = m_base.getAllEdges( sourceVertex, targetVertex );

        for( Iterator iter = edges.iterator(  ); iter.hasNext(  ); ) {
            Edge e = (Edge) iter.next(  );

            if( !containsEdge( e ) ) {
                m_edgeSet.add( e );

                return e;
            }
        }

        return null;
    }


    /**
     * Adds the specified edge to this subgraph. See performance discussion in
     * the class description.
     *
     * @param e the edge to be added.
     *
     * @return <code>true</code> if the edge was added, otherwise
     *         <code>false</code>.
     *
     * @throws NullPointerException
     * @throws IllegalArgumentException
     *
     * @see Subgraph
     * @see org._3pq.jgrapht.Graph#addEdge(Edge)
     */
    public boolean addEdge( Edge e ) {
        if( e == null ) {
            throw new NullPointerException(  );
        }

        if( !m_base.containsEdge( e ) ) {
            throw new IllegalArgumentException( NO_SUCH_EDGE_IN_BASE );
        }

        assert assertVertexExist( e.getSource(  ) );
        assert assertVertexExist( e.getTarget(  ) );
        assert assertBaseContainsEdgeInstance( e );

        if( containsEdge( e ) ) {
            return false;
        }
        else {
            m_edgeSet.add( e );

            return true;
        }
    }


    /**
     * Adds the specified vertex to this subgraph. See performance discussion
     * in the class description.
     *
     * @param v the vertex to be added.
     *
     * @return <code>true</code> if the vertex was added, otherwise
     *         <code>false</code>.
     *
     * @throws NullPointerException
     * @throws IllegalArgumentException
     *
     * @see Subgraph
     * @see org._3pq.jgrapht.Graph#addVertex(Object)
     */
    public boolean addVertex( Object v ) {
        if( v == null ) {
            throw new NullPointerException(  );
        }

        if( !m_base.containsVertex( v ) ) {
            throw new IllegalArgumentException( NO_SUCH_VERTEX_IN_BASE );
        }

        assertBaseContainsVertexInstance( v );

        if( containsVertex( v ) ) {
            return false;
        }
        else {
            m_vertexSet.add( v );

            return true;
        }
    }


    /**
     * @see org._3pq.jgrapht.Graph#containsEdge(Edge)
     */
    public boolean containsEdge( Edge e ) {
        return m_edgeSet.contains( e );
    }


    /**
     * @see org._3pq.jgrapht.Graph#containsVertex(Object)
     */
    public boolean containsVertex( Object v ) {
        return m_vertexSet.contains( v );
    }


    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    public int degreeOf( Object vertex ) {
        return ( (UndirectedGraph) m_base ).degreeOf( vertex );
    }


    /**
     * @see org._3pq.jgrapht.Graph#edgeSet()
     */
    public Set edgeSet(  ) {
        if( m_unmodifiableEdgeSet == null ) {
            m_unmodifiableEdgeSet = Collections.unmodifiableSet( m_edgeSet );
        }

        return m_unmodifiableEdgeSet;
    }


    /**
     * @see org._3pq.jgrapht.Graph#edgesOf(Object)
     */
    public List edgesOf( Object vertex ) {
        assert assertVertexExist( vertex );

        ArrayList edges     = new ArrayList(  );
        List      baseEdges = m_base.edgesOf( vertex );

        for( Iterator iter = baseEdges.iterator(  ); iter.hasNext(  ); ) {
            Edge e = (Edge) iter.next(  );

            if( containsEdge( e ) ) {
                edges.add( e );
            }
        }

        return edges;
    }


    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    public int inDegreeOf( Object vertex ) {
        return ( (DirectedGraph) m_base ).inDegreeOf( vertex );
    }


    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    public List incomingEdgesOf( Object vertex ) {
        return ( (DirectedGraph) m_base ).incomingEdgesOf( vertex );
    }


    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    public int outDegreeOf( Object vertex ) {
        return ( (DirectedGraph) m_base ).outDegreeOf( vertex );
    }


    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    public List outgoingEdgesOf( Object vertex ) {
        return ( (DirectedGraph) m_base ).outgoingEdgesOf( vertex );
    }


    /**
     * NOTE: We allow to remove an edge by the specified "value-equal" edge
     * that denotes it, which is not necessarily "reference-equal" to the edge
     * to be removed.
     *
     * @see org._3pq.jgrapht.Graph#removeEdge(Edge)
     */
    public boolean removeEdge( Edge e ) {
        return m_edgeSet.remove( e );
    }


    /**
     * @see org._3pq.jgrapht.Graph#removeEdge(Object, Object)
     */
    public Edge removeEdge( Object sourceVertex, Object targetVertex ) {
        Edge e = getEdge( sourceVertex, targetVertex );

        return m_edgeSet.remove( e ) ? e : null;
    }


    /**
     * NOTE: We allow to remove a vertex by the specified "value-equal" vertex
     * that denotes it, which is not necessarily "reference-equal" to the
     * vertex to be removed.
     *
     * @see org._3pq.jgrapht.Graph#removeVertex(Object)
     */
    public boolean removeVertex( Object v ) {
        if( containsVertex( v ) ) {
            removeAllEdges( edgesOf( v ) );
        }

        return m_vertexSet.remove( v );
    }


    /**
     * @see org._3pq.jgrapht.Graph#vertexSet()
     */
    public Set vertexSet(  ) {
        if( m_unmodifiableVertexSet == null ) {
            m_unmodifiableVertexSet =
                Collections.unmodifiableSet( m_vertexSet );
        }

        return m_unmodifiableVertexSet;
    }


    private void addEdgesUsingFilter( Set edgeSet, Set filter ) {
        Edge    e;
        boolean containsVertices;
        boolean edgeIncluded;

        for( Iterator iter = edgeSet.iterator(  ); iter.hasNext(  ); ) {
            e     = (Edge) iter.next(  );

            containsVertices =
                containsVertex( e.getSource(  ) )
                && containsVertex( e.getTarget(  ) );

            // note the use of short circuit evaluation            
            edgeIncluded = ( filter == null ) || filter.contains( e );

            if( containsVertices && edgeIncluded ) {
                addEdge( e );
            }
        }
    }


    private void addVerticesUsingFilter( Set vertexSet, Set filter ) {
        Object v;

        for( Iterator iter = vertexSet.iterator(  ); iter.hasNext(  ); ) {
            v = iter.next(  );

            // note the use of short circuit evaluation            
            if( filter == null || filter.contains( v ) ) {
                addVertex( v );
            }
        }
    }


    private boolean assertBaseContainsEdgeInstance( Edge e ) {
        if( !m_verifyIntegrity ) {
            return true;
        }

        List baseEdges = m_base.getAllEdges( e.getSource(  ), e.getTarget(  ) );

        for( Iterator i = baseEdges.iterator(  ); i.hasNext(  ); ) {
            Edge baseEdge = (Edge) i.next(  );

            if( e.equals( baseEdge ) ) {
                if( e != baseEdge ) {
                    throw new IllegalArgumentException( REF_NOT_EQUAL_TO_BASE );
                }

                return true;
            }
        }
        return true;
    }


    private void assertBaseContainsVertexInstance( Object v ) {
        if( !m_verifyIntegrity ) {
            return;
        }

        for( Iterator iter = m_base.vertexSet(  ).iterator(  );
                iter.hasNext(  ); ) {
            Object baseVertex = iter.next(  );

            if( v.equals( baseVertex ) ) {
                if( v != baseVertex ) {
                    throw new IllegalArgumentException( REF_NOT_EQUAL_TO_BASE );
                }

                return;
            }
        }
    }

    /**
     * An internal listener on the base graph.
     *
     * @author Barak Naveh
     *
     * @since Jul 20, 2003
     */
    private class BaseGraphListener implements GraphListener {
        /**
         * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
         */
        public void edgeAdded( GraphEdgeChangeEvent e ) {
            // we don't care
        }


        /**
         * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
         */
        public void edgeRemoved( GraphEdgeChangeEvent e ) {
            Edge edge = e.getEdge(  );

            removeEdge( edge );
        }


        /**
         * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
         */
        public void vertexAdded( GraphVertexChangeEvent e ) {
            // we don't care
        }


        /**
         * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
         */
        public void vertexRemoved( GraphVertexChangeEvent e ) {
            Object vertex = e.getVertex(  );

            removeVertex( vertex );
        }
    }
}
