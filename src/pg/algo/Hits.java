package pg.algo;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.DirectedGraph;

import pg.main.Progress;

/**
 * Applies the HITS algoritm.
 * HITS (Hyperlink-Induced Topic Search) allows to classify the nodes of a graph 
 * according to the structure of the graph. 
 * @author heinrich
 *
 */
public class Hits {
	
	public static Progress progress = new Progress() ;
	
	/**
	 * Precision of the algorithm. We stop when two consecutive scores have 
	 * a norme 1 distance below EPSILON
	 */
	private static double EPSILON = 1e-5 ; 

	public static <V extends Node,E> Map<V,Double> apply(DirectedGraph<V, E> g){
		double distance = 2. ;
		Map<V,Double> hubScore = new HashMap<>() ;
		Map<V,Double> authScore = new HashMap<>() ;
		
		for(V node : g.vertexSet())
		{
			hubScore.put(node, 1.0) ;
			authScore.put(node, 1.0) ;
		}
		
		normalize(hubScore) ;
		normalize(authScore) ;
		
		while(distance > EPSILON)
		{
			Map<V,Double> newHubScore = new HashMap<>() ;
			Map<V,Double> newAuthScore = new HashMap<>() ;
			
			for(V node : g.vertexSet())
			{
				newHubScore.put(node, 0.) ;
				newAuthScore.put(node, 0.) ;
			}
			
			for(V node : g.vertexSet())
			{
				double nodeAuthScore = 0. ;
				double nodeHubScore = 0. ;
				for(E edge : g.incomingEdgesOf(node))
					nodeAuthScore += g.getEdgeWeight(edge)*hubScore.get(g.getEdgeSource(edge)) ;
				
				for(E edge : g.outgoingEdgesOf(node))
					nodeHubScore += g.getEdgeWeight(edge) * authScore.get(g.getEdgeTarget(edge)) ;
				
				newHubScore.put(node, nodeHubScore) ;
				newAuthScore.put(node, nodeAuthScore) ;
			}
			
			normalize(newAuthScore) ;
			normalize(newHubScore) ;
			
			distance = Math.max(distance(authScore, newAuthScore), distance(hubScore, newHubScore)) ;
			authScore = newAuthScore ;
			hubScore = newHubScore ;
			
			System.out.println("HITS : iteration , distance : " + distance);
		}
		
		Map<V,Double> result = new HashMap<>() ;
		for(Map.Entry<V, Double> entry : authScore.entrySet())
		{
			result.put(entry.getKey(), Math.max(entry.getValue(), hubScore.get(entry.getKey()))) ;
			entry.getKey().description += "\nAuthority : " + entry.getValue() ;
			
			entry.getKey().description += "\nHub : " + hubScore.get(entry.getKey()) ;
		}
		
		
		return result;
	}
	
	private static <V> void normalize(Map<V, Double> score) {
		double total = 0 ;
		for(double d : score.values())
			total += d ;
		for(Map.Entry<V, Double> entry : score.entrySet())
			entry.setValue(entry.getValue() / total) ;
		return ;
	}
	
	private static <V> double distance(Map<V, Double> score1, Map<V, Double> score2){
		assert(score1.keySet().equals(score2.keySet())) ;
		double distance = 0 ;
		for(Map.Entry<V, Double> entry : score1.entrySet())
		{
			distance += Math.abs(entry.getValue() - score2.get(entry.getKey()));
		}
		return distance ;
	}
	
}
