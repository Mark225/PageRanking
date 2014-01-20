package pg.main;

import java.io.IOException;
import java.sql.SQLException;

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
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws  ParserConfigurationException, SAXException, TransformerException, IOException, SQLException {
		
		//new XmlDataHandler().run() ;
		
		new MainWindow() ;
	}
}
