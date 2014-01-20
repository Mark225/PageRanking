package pg.graphimport;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import pg.algo.Edge;
import pg.algo.Node;
import pg.main.Progress;

/**
 * Class to import graph using queries on a local wikipedia database.
 * @author heinrich
 *
 */
public class WikipediaImporter {
	
	/**
	 * Maximum number of results to use from the research on CiteSteer.
	 */
	public static int BASIC_NUM = 30 ;
	public static boolean ONLY_ARTICLE = false ;
	
    private Connection con ;
    private PreparedStatement inNodesStat ;
    private PreparedStatement idToTitleStat ;
    private PreparedStatement queryStat ;
    private PreparedStatement outNodesStat ;
    private PreparedStatement titleToIdStat ;
    private PreparedStatement onlyTitleToId ;
    
    
    private String url = "jdbc:mysql://localhost:3306/socnet";
    private String user = "root";
    private String password = "Sweg+ob5" ;
    private String linktb = "pagelinks" ;
    private String nametb = "page" ;
    private String descrtbl = "description" ;
	private Progress progress ;
	
	
	public WikipediaImporter(Progress progress) throws SQLException {
		this.progress = progress ;
		con = DriverManager.getConnection(url, user, password) ;
		inNodesStat = con.prepareStatement("SELECT * FROM " + linktb + " WHERE pl_namespace = ? AND pl_title = ?") ;
		idToTitleStat = con.prepareStatement("SELECT * FROM " + nametb + " WHERE page_id = ?") ;
		queryStat = con.prepareStatement("SELECT * FROM " + descrtbl + " WHERE MATCH (title, abstract)  AGAINST (?)  LIMIT 0, ?" ) ;
		outNodesStat = con.prepareStatement("SELECT * FROM " + linktb + " WHERE pl_from = ? ") ;
		titleToIdStat = con.prepareStatement("SELECT * FROM " + nametb + " WHERE page_title = ? AND page_namespace = ?") ;
		onlyTitleToId = con.prepareStatement("SELECT * FROM " + nametb + " WHERE page_namespace = 0 AND page_title = ?") ;
	}
	
	public DirectedGraph<Node, Edge> importGraph(String search) throws SQLException, IOException {
		
		progress.info("Send Query to database") ;
		Set<Integer> coreSet = sendQuery(search) ;
		Set<Integer> initSet = new HashSet<>(coreSet) ;
		
		progress.info("Initial number of nodes : " + coreSet.size()) ;
		progress.info("Adding adjacent nodes") ;
		progress.setProgress(0) ;
		//Adding nodes to get a base Set.
		List<Integer> copy = new LinkedList<>(coreSet) ;
		int j = 0 ;
		for(int i : copy)
		{
			progress.setProgress(j++ , copy.size()) ;
			coreSet.addAll(getInNodes(i)) ;
			coreSet.addAll(getOutNodes(i)) ;
		}
		
		DirectedGraph<Node, Edge> g = new DefaultDirectedWeightedGraph<Node, Edge>(Edge.class) ;
		for(int i : coreSet)
		{
			Node n = new Node(Integer.toString(i)) ;
			g.addVertex(n) ;
			if(initSet.contains(i))
				n.description +="(init)" ;
		}
		
		progress.info("Total number of vertices : " + g.vertexSet().size()) ;
		progress.info("Adding edges") ;
		progress.setProgress(0) ;
		//Adding edges
		int i = 0 ;
		for(Node n : g.vertexSet())
		{
			i++ ;
			progress.setProgress(i, g.vertexSet().size()) ;
			addEdges(g,n) ;
		}
		
		(new FileImporter(progress)).exportGraph(g, "auto-save wikipediaimport search=" + search + " [bset: " + BASIC_NUM + ", " + ONLY_ARTICLE + "]") ;
		
		return g ;
	}

	private void addEdges(DirectedGraph<Node, Edge> g, Node n) throws SQLException {
		int id = Integer.parseInt(n.getName()) ;
		idToTitleStat.setInt(1, id) ;
		ResultSet res = idToTitleStat.executeQuery() ;
		if(!res.next())
			assert(false) ;
		byte[] title = res.getBytes("page_title") ;
		int namespace = res.getInt("page_namespace") ;
		n.description += new String(title) ;
		
		inNodesStat.setBytes(2, title) ;
		inNodesStat.setInt(1, namespace) ;
		ResultSet res2 = inNodesStat.executeQuery() ;
		int i = 0 ;
		while(res2.next())
		{
			i ++ ;
			int source = res2.getInt("pl_from") ;
			Node n2 = new Node(Integer.toString(source)) ;
			if(g.containsVertex(n2))
				g.addEdge(n2, n) ;
		}
		if(i == 0)
			System.out.println("Not found (inNode) : " + new String(title) + "----" + inNodesStat.toString() );
	}

	private Collection<Integer> getOutNodes(int i) throws SQLException {
		List<Integer> l = new LinkedList<>() ;
		outNodesStat.setInt(1, i) ;
		ResultSet res = outNodesStat.executeQuery() ;
		while(res.next())
		{
			if(!ONLY_ARTICLE || res.getInt("pl_namespace") == 0)
			{
				titleToIdStat.setInt(2, res.getInt("pl_namespace")) ;
				titleToIdStat.setBytes(1, res.getBytes("pl_title")) ;
				ResultSet res2 = titleToIdStat.executeQuery() ;
				if(res2.next())
					l.add(res2.getInt("page_id")) ;
				else 
					System.out.println("Not found (titleToId) : " + res.getString("pl_title") ) ;
			}
		}
			
		return l;
	}

	private Collection<Integer> getInNodes(int i) throws SQLException {
		List<Integer> l = new LinkedList<>() ;
		idToTitleStat.setInt(1, i) ;
		ResultSet res = idToTitleStat.executeQuery() ;
		if(!res.next())
			assert(false) ;
		
		byte[] title = res.getBytes("page_title") ;
		int namespace = res.getInt("page_namespace") ;
		inNodesStat.setInt(1, namespace) ;
		inNodesStat.setBytes(2, title) ;
		ResultSet res2 = inNodesStat.executeQuery() ;
		while(res2.next())
		{
			int id = res2.getInt("pl_from") ;
			if(!ONLY_ARTICLE)
				l.add(id) ;
			else 
			{
				idToTitleStat.setInt(1, id) ;
				ResultSet res3 = idToTitleStat.executeQuery() ;
				if(!res3.next())
					assert(false) ;
				if(res3.getInt("page_namespace") == 0)
					l.add(id) ;
			}
		}
			
		return l;
	}

	private Set<Integer> sendQuery(String keyword) throws SQLException {
		queryStat.setString(1, keyword) ;
		queryStat.setInt(2, BASIC_NUM) ;
		ResultSet res = queryStat.executeQuery() ;
		Set<Integer> s = new HashSet<>() ;
		while(res.next())
		{
			progress.setProgress(res.getRow(),BASIC_NUM) ;
			String title =res.getString("title") ;
			title = title.replace(' ', '_') ;
			
			onlyTitleToId.setBytes(1, title.getBytes()) ;
			ResultSet res2 = onlyTitleToId.executeQuery() ;
			int i = 0 ;
			while(res2.next())
			{
				s.add(res2.getInt("page_id")) ;
				i++ ;
			}
			if(i==0)
			{
				System.out.println("Not found : " + title + "Query = " + onlyTitleToId.toString());	
			}
		}
		
		return s;
	}

	
}
