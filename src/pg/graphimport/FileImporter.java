package pg.graphimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

import org.jgrapht.DirectedGraph;

import pg.algo.Edge;
import pg.algo.Node;
import pg.main.Progress;

/**
 * Class providing a method to import and export graphs from a file.
 * @author heinrich
 *
 */
public class FileImporter {
	
	
	public static Path basePath ;
	static {
		URL url = FileImporter.class.getProtectionDomain()
		        .getCodeSource().getLocation() ;
		basePath = Paths.get(url.getPath()).getParent();
		while(!basePath.endsWith("PageRanking"))
			basePath = basePath.getParent() ;
	}
	
	private File directory ;
	@SuppressWarnings("unused")
	private Progress progress;
	
	public FileImporter(Progress progress){
		directory = basePath.resolve("data" + File.separator + "graph").toFile() ;
		this.progress = progress ;
	}
	
	public FileImporter(Path p, Progress progress){
		this.progress = progress ;
		if(p.isAbsolute())
		{
			directory = p.toFile() ;
		}
		else
		{
			directory = basePath.resolve(p).toFile() ;
		}
	}

	public DirectedGraph<Node, Edge> importGraph(final String search) throws IOException, ClassNotFoundException {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equals(search);
			}
		} ;
		File f = directory.listFiles(filter)[0] ;
		FileInputStream fileinput = new FileInputStream(f) ;
		ObjectInputStream graph_input = new ObjectInputStream(fileinput) ;
		
		@SuppressWarnings("unchecked")
		DirectedGraph<Node, Edge> g = (DirectedGraph<Node, Edge>) graph_input.readObject() ;
		graph_input.close() ;
		fileinput.close() ;
		return g;
	}

	public Vector<String> availableFiles() {
		Vector<String> vect = new Vector<String>() ;
		for(File f : directory.listFiles())
			vect.add(f.getName());
		return vect;
	}

	public void exportGraph(DirectedGraph<Node, Edge> g, String name) throws IOException{
		FileOutputStream outs = new FileOutputStream(new File(directory, name)) ;
		ObjectOutputStream out_o = new ObjectOutputStream(outs) ;
		out_o.writeObject(g) ;
		out_o.close() ;
		outs.close() ;
	}
}
