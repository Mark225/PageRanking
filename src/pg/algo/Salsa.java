package pg.algo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.WeightedPseudograph;


/**
 * Class to apply the Salsa algorithm on the given graph.
 * @author heinrich
 */
public class Salsa {
	
	/**
	 * Applies the SALSA algorithm on the graph. Returns a value for each of
	 * the nodes of the graph corresponding to its ranking.
	 * @param g : the graph to apply the algorithm on.
	 * @return
	 */
	public static <V,E> Map<V, Double> apply(DirectedGraph<V,E> g){
		Map<V,Double> resAuth = applyAuth(g);
		Map<V,Double> resHub = applyHub(g);
		Map<V,Double> result = new HashMap<>() ;
		for(V n : g.vertexSet())
		{
			double vertex_value = 0 ;
			if(resAuth.get(n) != null)
				vertex_value = Math.max(vertex_value, resAuth.get(n)) ;
			if(resHub.get(n) != null)
				vertex_value = Math.max(vertex_value, resHub.get(n)) ;
			result.put(n, vertex_value) ;
		}
		return result ;
	}

	/**
	 * Applies the salsa algorithm seen as authority ranking. 
	 * @param g : the graph to apply the algorithm on
	 * @return A map containing the score of each node with the algorithm
	 * The score can be used to sort the Nodes.
	 */
	private static <V,E> Map<V, Double> applyAuth(DirectedGraph<V,E> g) {
		
		// -> compute d_in and d_out
		Map<V, Double> degIn = new HashMap<>() ;
		Map<V, Double> degOut = new HashMap<>() ;
		
		for (V n : g.vertexSet()) {
			degIn.put(n, 0.) ;
			degOut.put(n, 0.) ;
			
			for(E e : g.edgesOf(n))
			{
				if(g.getEdgeSource(e).equals(n))
				{
					degOut.put(n, degOut.get(n) + g.getEdgeWeight(e)) ;
				}
				if(g.getEdgeTarget(e).equals(n))
				{
					degIn.put(n, degIn.get(n) + g.getEdgeWeight(e)) ;
				}
			}
		}
		
		//Creates a new graph corresponding to the Markov chain
		WeightedPseudograph<V, DefaultWeightedEdge> tempGraph ; 
		tempGraph = new WeightedPseudograph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class) ;
		
		// -> put in the graph all nodes with d_in > 0 (authorities)
		for(V n : g.vertexSet())
		{
			if(degIn.get(n) != 0.)
				tempGraph.addVertex(n) ;
		}
		
		// -> add all the required edges (and compute their weight)
		for(V auth1 : tempGraph.vertexSet())
		{
			for(E edge1 : g.incomingEdgesOf(auth1))
			{
				V hub1 = g.getEdgeSource(edge1) ;
				for(E edge2 : g.outgoingEdgesOf(hub1))
				{
					V auth2 = g.getEdgeTarget(edge2) ;
					if(!tempGraph.containsEdge(auth1, auth2))
					{
						DefaultWeightedEdge newEdge = tempGraph.addEdge(auth1, auth2) ;
						tempGraph.setEdgeWeight(newEdge, 0) ;
					}
					/* No need to compute weights...
					DefaultWeightedEdge e = tempGraph.getEdge(auth1, auth2) ;
					double newWeight = tempGraph.getEdgeWeight(e) ; 
					newWeight += g.getEdgeWeight(edge1) * g.getEdgeWeight(edge2) 
							/ (degIn.get(auth1) * degOut.get(hub1)) ;
					tempGraph.setEdgeWeight(e, newWeight) ;
					*/
				}
			}
		}		
		
		//Compute the value for each node (independently of ponderation)
		Map<V, Double> authRes = new HashMap<>(degIn) ;
		/*double total = 0f ;
		for(double val : authRes.values())
		{
			total += val ;
		}
		for(V n : authRes.keySet())
		{
			authRes.put(n, authRes.get(n) / total) ;
		}*/
		
		//Compute the connected components
		ConnectivityInspector<V, DefaultWeightedEdge> connectivity = new ConnectivityInspector<V, DefaultWeightedEdge>(tempGraph) ;
		List<Set<V>> components = connectivity.connectedSets() ;
		
		//Ponderate these values by the size of the connected component.
		int number_nodes = tempGraph.vertexSet().size() ;
		for(Set<V> comp : components)
		{
			double total_comp = 0.0 ;
			for(V n : comp)
			{
				for(E e : g.incomingEdgesOf(n))
				{
					total_comp += g.getEdgeWeight(e) ;
				}
			}
			
			for(V n : comp)
			{
				authRes.put(n, authRes.get(n)/total_comp*comp.size()/number_nodes) ;
			}
		}
		
		return authRes;
	}
	
	/**
	 * Applies the salsa algorithm seen as hub ranking. 
	 * @param g : the graph to apply the algorithm on
	 * @return A map containing the score of each node with the algorithm
	 * The score can be used to sort the Nodes.
	 */
	private static <V,E> Map<V, Double> applyHub(DirectedGraph<V,E> g) {
		return applyAuth(new EdgeReversedGraph<V,E>(g)) ;
	}
	
}
