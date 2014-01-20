package pg.graphimport;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.jgrapht.DirectedGraph;

import pg.algo.Edge;
import pg.algo.Node;
import pg.main.Progress;

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
		FROM_FILE, 
		LIBRA, 
		WIKIPEDIA_FR
	}
	
	/**
	 * Imports a graph with the given mode. The search string must be a sequence of keywords 
	 * separated by '+', and without any whitespace.
	 * @param mode
	 * @param search
	 * @return
	 * @throws IOException 
	 */
	
	private CiteSeerImporter citeseerImport ;
	private FileImporter fileImport  ;
	private LibraImporter libraImport ;
	private WikipediaImporter wikipediaImporter ;
	private Progress progress ;
	private boolean wikiAvailable = true ;
	
	public GraphImport(Progress progress) {
		this.progress = progress ;
		citeseerImport = new CiteSeerImporter(progress) ;
		fileImport = new FileImporter(progress)  ;
		libraImport = new LibraImporter(progress) ;
		try {
			wikipediaImporter = new WikipediaImporter(progress) ;
		} catch (SQLException e) {
			progress.info("WikipediaImport not available") ;
			wikiAvailable = false ;
		}
	}
	
	public DirectedGraph<Node, Edge> importFrom(Mode mode, String search) throws IOException, ClassNotFoundException, InterruptedException, SQLException{
		
		progress.info("Importing graph") ;
		DirectedGraph<Node, Edge> g = null ; 
		
		switch(mode){
		case CITESEERX : 
			g =citeseerImport.importGraph(search) ;
			break ;
		case FROM_FILE:
			g = fileImport.importGraph(search) ;
			break;
		case LIBRA : 
			g = libraImport.importGraph(search) ;
			break ;
		case WIKIPEDIA_FR : 
			if(wikiAvailable)
				g = wikipediaImporter.importGraph(search) ;
			else 
			{
				progress.error("Wikipedia importer not available :") ;
				progress.error("The database is probably not installed in your computer ") ;
				throw new SQLException("Database not installed") ;
			}
			break ;
		}
		
		return g;
	}

	/**
	 * Gets the list of the available file to import a graph.
	 * This is the list of correct search fields for the FROM_FILE import mode
	 * @return
	 */
	public Vector<String> availableFiles() {
		return fileImport.availableFiles();
	}

	public void exportGraph(DirectedGraph<Node, Edge> g, String name) throws IOException{
		fileImport.exportGraph(g, name);
	}

}
