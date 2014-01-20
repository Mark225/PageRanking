package pg.graphimport;

import java.io.IOException;
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




/**
 * @author heinrich
 * Rq : Partie citation : articles cités dans le document
 * cited by : article citant ce document
 */
public class CiteSeerImporter {
	
	/**
	 * timeout for read operations.
	 */
	private static final int TIMEOUT = 10000;

	/**
	 * Maximum number of results to use from the research on CiteSteer.
	 */
	public static int BASIC_NUM = 30 ;
	private Progress progress;
	
	public CiteSeerImporter(Progress progress){
		this.progress = progress ;
	}
	
	public DirectedGraph<Node, Edge> importGraph(String keyword) throws IOException{
		
		DirectedGraph<Node, Edge> g = new DefaultDirectedGraph<Node, Edge>(Edge.class) ;
		
		//Create the base set of URL(s)
		List<String> urls = new LinkedList<String>() ;
		for(int i = 0 ; i < BASIC_NUM/10 ; i ++)
		{
			urls.add("http://citeseerx.ist.psu.edu/search?q=" + keyword + "&sort=rlv&start=" + 10*i) ;
		}
		int i = 1 ;
		
		for(String address : urls)
		{		
			List<String> l = parseSearchResults(address) ;
			for(String name : l)
			{
				Node n = new Node(name) ;
				if(! g.containsVertex(n))
				{
					g.addVertex(n) ;
					n.description += "(" + i +")" ;
					i += 1 ;
					System.out.println("Initial vertice : " + n.getName()) ;
				}
			}
		}
		
		progress.info("Number of initial vertices : " + g.vertexSet().size()) ;
		
		//Search each URL from now, and add all the links and the Nodes pointed by these 
		// links if necessary.
		//copy because the loop will add nodes, and we don't want to consider theses 
		// nodes in the loop
		int k = 0 ;
		for(Node n : new HashSet<Node>(g.vertexSet()))
		{
			progress.setProgress(k, BASIC_NUM) ;
			addNodes(n, g) ;
			k++ ;
		}
		
		progress.info("Number of nodes : " + g.vertexSet().size()) ;
		addEdges(g) ;
		System.out.println("Import finished") ;
		
		new FileImporter(progress).exportGraph(g, "auto-save citeseerimport search=" + keyword +" [bset:" + BASIC_NUM + "]") ;
		
		return g;
	}

	/**
	 * Parses the document at the given address corresponding to a certain query 
	 * on citeseerx, and gives a list of the list for each result.
	 * @param address : the address of the webpage to parse
	 * @return a list of ids.
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	private List<String> parseSearchResults(String address) {
		try 
		{
			List<String> l = new LinkedList<>() ;

			Document doc = Jsoup.connect(address).timeout(TIMEOUT).get() ;
			Element body = doc.body() ;

			Element res_list = body.getElementById("result_list") ;
			Elements titles = res_list.getElementsByClass("doc_details") ;
			for(Element article : titles)
			{
				String id = createDocId(article.attr("abs:href")) ;
				if(id != null)
				{
					l.add(id) ;
				}
				else 
					System.out.println("Address ignored :" + article.attr("abs:href")) ;

			}
			return l ;
		}
		catch(IOException e) {
			progress.error("Server too long to respond") ;
			Thread.currentThread().stop() ;
			return new LinkedList<>() ;
		}
	}


	private void addNodes(Node n, DirectedGraph<Node, Edge> g) throws IOException {

		String name = n.getName() ;
		//Get the documents cited by this article
		Document doc = null ;
		try {
			doc = Jsoup.connect("http://citeseerx.ist.psu.edu/viewdoc/summary?doi=" + name).timeout(TIMEOUT).get() ;
		} catch(IOException e) {
			System.out.println("Caught IO exception, retry");
			addNodes(n,g) ;
			return ;
		}
		n.description += getDescription(doc) ;
		Element citations = doc.body().getElementById("citations") ;
		for(Element link : citations.select("a[href]"))
		{
			
			Document doc_article = null ;
			try {
				doc_article = Jsoup.connect(link.attr("abs:href")).timeout(TIMEOUT).get() ;
			} catch (IOException e) {
				System.out.println("caught IOException, some nodes may be missing") ;
				progress.warning("Server too long to respond, some nodes may be missing") ;
				continue ;
			}
			String id = createDocId(doc_article.baseUri()) ;
			if(id == null || doc_article.title().contains("Document Removed"))
			{
				//Document not in the database (not a doi address), or 
				//removed from database
				// This document is ignored.
			}
			else 
			{
				//DOI document, continue analysis
				Node n2 = new Node(id) ;
				if(!g.containsVertex(n2))
				{
					//Add the vertex only if it is not already in the graph.
					g.addVertex(n2) ;
					n2.description += getDescription(doc_article) ;
					System.out.println("Added Vertice : " + n2.getName()) ;
				}
			}
		}
		
		//Get the documents that cite this article
		Document doc2 = null ;
		int i = 10 ;
		int k = 0 ;
		while(i ==10)
		{
			try {
				doc2 = Jsoup.connect("http://citeseerx.ist.psu.edu/showciting?doi=" + name + "&start=" + (10*k)).timeout(TIMEOUT).get() ;
			} catch (IOException e) {
				System.out.println("caught IOException, some nodes may be missing") ;
				continue ;
			}
			k += 1 ;
			Element cite_list = doc2.body().getElementById("result_list") ;
			Elements links = cite_list.getElementsByClass("doc_details") ;
			i = links.size() ;
			for(Element link : links)
			{
				String link_name = createDocId(link.attr("abs:href")) ;
				Node n1 = new Node(link_name) ;
				if(!g.containsVertex(n1))
				{
					g.addVertex(n1) ;
					System.out.println("Added Vertice : " + n1.getName()) ;
				}
				
			}
		}
		System.out.println("Node finished");
	}

	private void addEdges(DirectedGraph<Node, Edge> g) throws IOException{
		
		progress.info("Adding edges to the graph") ;
		progress.setProgress(0) ;
		int prog = 0 ;
		for(Node n1 : g.vertexSet())
		{
			progress.setProgress(prog, g.vertexSet().size()) ;
			prog ++ ;
			int i = 10 ;
			int k = 0 ;
			while(i == 10)
			{
				Document node_doc = null ;
				try {
					node_doc = Jsoup.connect("http://citeseerx.ist.psu.edu/showciting?doi=" + n1.getName() + "&start=" + (10*k)).timeout(TIMEOUT).get() ;
				} catch(IOException e) {
					System.out.println("caught IOException, some links may be missing");
					progress.warning("Server too long to respond, some links may be missing") ;
					continue ;
				}
				if(n1.description.equals(""))
					n1.description += getDescription(node_doc) ;
				Elements links = node_doc.body().getElementById("result_list").getElementsByClass("doc_details") ;
				i = links.size() ;
				k += 1 ;
				for(Element link : links)
				{
					String name2 = createDocId(link.attr("abs:href")) ;
					if(name2 != null)
					{
						Node n2 = new Node(name2) ;
						if(g.containsVertex(n2))
						{
							g.addEdge(n1, n2) ;
							System.out.println("Added Edge : (" + n1.getName() + ", " + n2.getName() + ")") ;
						}
					}
				}
			}
			
		}
		
	}
	
	/**
	 * Creates an id for each document. 
	 * When the address has the form 
	 * http://citeseerx.ist.psu.edu/viewdoc/summary;jsessionid=54DABEDFDF38C963D9ED589935031486?doi=10.1.1.307.9272
	 * the id will be 10.1.1.307.9272.
	 * @param address : the address of the document
	 * @return an id for the document : null if the address has not the correct format.
	 * The id will be the DOI (digital object identifier) of the 
	 */
	private static String createDocId(String address){
		int i = 0 ;
		while(i+3 < address.length()-1)
		{
			if(address.substring(i, i+4).equals("doi="))
			{
				return address.substring(i+4) ;
			}
			i +=1 ;
		}
		
		return null ;
	}
	
	/**
	 * Parses the document to extract a description of a given node.
	 * @param doc (HTML doc, summary type)
	 * @return a string giving informations on a particular article
	 */
	private String getDescription(Document doc) {
		
		Element titleEl = doc.select("meta[name=citation_title]").first() ;
		String title = "??" ;
		if(titleEl != null)
			title = titleEl.attr("content") ;
		
		Element authorsEl = doc.select("meta[name=citation_authors]").first() ;
		String authors = "??" ;
		if(authorsEl != null)
			authors = authorsEl.attr("content") ;
		String url = doc.location() ;
		
		Element yearEl = doc.select("meta[name=citation_year]").first() ;
		String year = "??" ;
		if(yearEl != null)
			year = yearEl.attr("content") ;
		return "Title : " + title + "\nAuthors : " + authors + "\nYear : " + year + "\nURL : " + url  ;
	}

}
