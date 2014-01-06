package pg.graphimport;

import java.io.IOException;
import java.util.Vector;

import org.jgrapht.DirectedGraph;

import pg.algo.Edge;
import pg.algo.Node;

/**
 * Class to import graph from data in the Internet.
 * It provides several ways to import a graph.
 * The Enum gives the possibilities
 * @author heinrich
 *
 */
public class GraphImport {
	
	public enum Mode {
		CITESEERX, 
		FROM_FILE
	}
	
	/**
	 * Imports a graph with the given mode. The search string must be a sequence of keywords 
	 * separated by '+', and without any whitespace.
	 * @param mode
	 * @param search
	 * @return
	 * @throws IOException 
	 */
	
	private static CiteSeerImporter citeseerImport = new CiteSeerImporter();
	private static FileImporter fileImport = new FileImporter() ;
	
	public static DirectedGraph<Node, Edge> importFrom(Mode mode, String search) throws IOException, ClassNotFoundException{
		
		DirectedGraph<Node, Edge> g = null ; 
		
		switch(mode){
		case CITESEERX : 
			g =citeseerImport.importGraph(search) ;
			break ;
		case FROM_FILE:
			g = fileImport.importGraph(search) ;
			break;
		}
		
		return g;
	}

	/**
	 * Gets the list of the available file to import a graph.
	 * This is the list of correct search fields for the FROM_FILE import mode
	 * @return
	 */
	public static Vector<String> availableFiles() {
		return fileImport.availableFiles();
	}

	public static void exportGraph(DirectedGraph<Node, Edge> g, String name) throws IOException{
		fileImport.exportGraph(g, name);
	}

}
