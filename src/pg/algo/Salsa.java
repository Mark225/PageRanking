package pg.algo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.util.UnionFind;
import org.jgrapht.graph.EdgeReversedGraph;

import pg.main.Progress;


/**
 * Class to apply the SALSA algorithm on the given graph.
 * SALSA (Stochastic Approach for Link Structure Analysis) is a an algorithm used to 
 * classify result when performing search on linked structures.
 * Given a graph, it returns a score for each of its node, depending only of the structure
 * of the graph.
 * @author heinrich
 */
public class Salsa {
	
	public static Progress progress = new Progress() ;
	
	/**
	 * Applies the SALSA algorithm on the graph. Returns a value for each of
	 * the nodes of the graph corresponding to its ranking.
	 * @param g : the graph to apply the algorithm on.
	 * @return
	 */
	public static <V extends Node ,E> Map<V, Double> apply(DirectedGraph<V,E> g){
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
			n.description += "\nAuthorité : " + resAuth.get(n) ; 
			n.description += "\nHub : " + resHub.get(n) ;
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
		
		UnionFind<V> connectedComp = new UnionFind<>(new HashSet<V>()) ;
		
		for(Map.Entry<V, Double> entry : degIn.entrySet())
		{
			if(entry.getValue() != 0.)
				connectedComp.addElement(entry.getKey()) ;
		}
		
		for(V n : g.vertexSet())
		{
			Iterator<E> it = g.outgoingEdgesOf(n).iterator() ;
			if(it.hasNext())
			{
				E e1 = it.next() ;
				while(it.hasNext())
				{
					E e2 = it.next();
					connectedComp.union(g.getEdgeTarget(e1), g.getEdgeTarget(e2)) ;
				}
			}
		}
		
		Map<V,Set<V>> mapComponents = new HashMap<>() ;
		for(V n : g.vertexSet())
		{
			if(degIn.get(n) != 0.)
			{
				V nref = connectedComp.find(n) ;
				if(mapComponents.containsKey(nref))
					mapComponents.get(nref).add(n) ;
				else 
				{
					Set<V> tempset = new HashSet<>() ;
					tempset.add(n) ;
					mapComponents.put(nref, tempset) ;
				}
			}
		}
		
		Collection<Set<V>> components = mapComponents.values() ; 
		
		progress.info("Number of components : " + components.size()) ;
		
		//Compute the value for each node (independently of ponderation)
		Map<V, Double> authRes = new HashMap<>(degIn) ;
		
		//Ponderate these values by the size of the connected component.
		int number_nodes = 0 ;
		for(Set<V> comp : components)
			number_nodes += comp.size() ;
			
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
