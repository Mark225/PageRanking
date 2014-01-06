package pg.main;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class Main {
	
	public static String PROG_NAME = "PageRanking/1.0" ;

	/**
	 * @param args : TODO
	 * @throws TransformerException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws  ParserConfigurationException, SAXException, TransformerException, IOException {
		
		//Test.test() ;
		
		/*MainWindow win = */new MainWindow() ;
		
		 	/*	
		DirectedGraph<Node, Edge> graph = GraphImport.importFrom(GraphImport.Mode.CITESEERX, "k-server") ; 
		
		Map<Node, Double> result = Salsa.apply(graph) ;
		printResult(result) ; */
	}
/*
	private static void printResult(Map<Node, Double> result) {
		for(Map.Entry<Node, Double> val : result.entrySet())
		{
			System.out.println(val.getKey().getName() + " : " + val.getValue()) ;
		}
	}
*/
}
