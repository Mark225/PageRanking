package pg.graphimport;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pg.algo.Edge;
import pg.algo.Node;
import pg.main.Progress;

public class LibraImporter {
	
	private Progress progress ;
	
	/**
	 * timeout for read operations.
	 */
	private static final int TIMEOUT = 10000;
	
	private static int SLEEP = 10000 ;
	
	/**
	 * Maximum number of results to use from the research on CiteSteer.
	 */
	public static int BASIC_NUM = 30 ;
	
	public LibraImporter(Progress progress) {
		this.progress = progress ;
	}

	
	public DirectedGraph<Node, Edge> importGraph(final String search) throws IOException, InterruptedException {
		progress.setProgress(0) ;
		DirectedGraph<Node, Edge> g = new DefaultDirectedGraph<Node, Edge>(Edge.class) ;

		//Create the base set of vertices
		int position = 1 ; 
		String searchUrl = "http://academic.research.microsoft.com/Search?query=" + search + "&start=1&end=" + BASIC_NUM ;
		Document doc = Jsoup.connect(searchUrl).timeout(TIMEOUT).get() ;
		Elements elts = doc.getElementsByClass("title-download") ;
		for(Element el : elts)
		{
			for(Element link : el.getElementsByAttribute("abs:href")) 
			{
				String eltLink = link.attr("abs:href") ;
				 if(eltLink.startsWith("http://academic.research.microsoft.com/Publication/"))
				 {
					 String name = eltLink.substring(51) ;
					 Node n = new Node(name) ;
					 n.description += "(" + position + ")" ;
					 position ++ ;
					 g.addVertex(n) ;
					 System.out.println("Initial vertice : " + n) ;
				 }
			}
		}
		Thread.sleep(SLEEP) ;
		
		progress.info("initial number of nodes : " + g.vertexSet().size()) ;
		progress.info("Adding nodes") ;
		//Adding nodes
		int nb = 0 ;
		for(Node n : new HashSet<>(g.vertexSet()))
		{
			nb ++ ;
			progress.setProgress(nb, BASIC_NUM) ;
			Collection<Node> l = parseFromUrl("http://academic.research.microsoft.com/Detail?entitytype=1&searchtype=2&id=" + getId(n.getName())) ;
			for(Node baseN : l)
			{
				if(g.addVertex(baseN))
					System.out.println("Added Node : " + baseN) ;

			}
			l = parseFromUrl("http://academic.research.microsoft.com/Detail?entitytype=1&searchtype=5&id=" + getId(n.getName())) ;
			for(Node destN : l)
			{
				if(g.addVertex(destN))
					System.out.println("Added Node : " + destN) ;
			}
			Thread.sleep(SLEEP) ;
		}
		
		
		//adding edges
		progress.info("adding edges") ;
		nb = 0 ;
		for(Node nodeBase : g.vertexSet())
		{
			nb ++ ;
			progress.setProgress(nb, g.vertexSet().size()) ;
			Collection<Node> l = parseFromUrl("http://academic.research.microsoft.com/Detail?entitytype=1&searchtype=2&id=" + getId(nodeBase.getName())) ;
			for(Node nodeDest : l)
			{
				if(g.containsVertex(nodeDest))
				{
					g.addEdge(nodeBase, nodeDest) ;
					System.out.println("Added Edge (" + nodeBase + ", " + nodeDest + ")") ;
				}
			}
			Thread.sleep(SLEEP) ;
		}
		
		(new FileImporter(progress)).exportGraph(g, "auto-save libraImport search=" + search + " [bset:" + BASIC_NUM + "]") ;
		
		return g ;
	}
	
	private Collection<Node> parseFromUrl(String baseUrl) throws IOException{
		int num_link = 100 ;
		int k = 0 ;
		
		List<Node> l = new LinkedList<>() ;
		try{
		while(num_link == 100)
		{
			System.out.println(baseUrl + "&start="+ (k*num_link +1) +"&end=" +(k+1)*num_link) ;
			Document references = Jsoup.connect(baseUrl + "&start="+ (k*num_link +1) +"&end=" +(k+1)*num_link).timeout(TIMEOUT).get() ;
			num_link = 0 ;
			k ++ ;
			Elements titles = references.getElementsByClass("title-download") ;
			if(titles.size() == 0)
				System.out.println(references.baseUri()) ;
			for(Element title : titles)
			{
				for(Element linkElt : title.getElementsByAttribute("abs:href"))
				{
					String link = linkElt.attr("abs:href") ;
					 if(link.startsWith("http://academic.research.microsoft.com/Publication/"))
					 {
						 String name = link.substring(51) ;
						 l.add(new Node(name)) ;
						 num_link ++ ;
					 }
				}
			}	
		}
		} catch (IOException e) {
			progress.warning("Server too long to respond, page ignored") ;
			return l ; 
		}
		return l ;
	}
	
	
	private String getId(String name){
		int i = 0 ;
		while(i < name.length() -1 && name.charAt(i) != '/')
			i ++ ;
		return name.substring(0,i) ;
	}
}
