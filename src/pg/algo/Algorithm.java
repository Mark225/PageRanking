package pg.algo;

import java.util.Map;

import org.jgrapht.DirectedGraph;

public class Algorithm {

	public enum Type {
		/**
		 * The SALSA algorithm
		 */
		SALSA, 
		
		/**
		 * The HITS  algorithm
		 */
		HITS
	}
	
	public static <V extends Node,E> Map<V,Double> apply(Type algo, DirectedGraph<V, E> g){
		
		Map<V,Double> result = null ;
		switch (algo) {
			case SALSA:
				result = Salsa.apply(g) ;
				break ;
			
			case HITS :
				result = Hits.apply(g) ;
				break ;
		}
		return result ;
	}
	
}
