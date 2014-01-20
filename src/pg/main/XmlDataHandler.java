package pg.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import pg.graphimport.FileImporter;

public class XmlDataHandler extends DefaultHandler {
	
	StringBuilder buff = new StringBuilder() ;
	
	
	boolean fillBuffer = false ; 
	
	String title ;
	String abstr ;
	
    Connection con = null;
    PreparedStatement push = null ;
    int nb_pending = 0 ;
    int nb_sent = 1000 ;
    
    int nb_lines = 0 ;
    
    String url = "jdbc:mysql://localhost:3306/socnet";
    String user = "root";
    String password = "Sweg+ob5";
    String table = "description";
    
    public XmlDataHandler() throws SQLException {
		con = DriverManager.getConnection(url, user, password) ;
		String s = "INSERT INTO " + table + " VALUES (?, ?)"  ;
		
		for(int i = 1 ; i <= nb_sent -1 ; i++)
			s += ", (?, ?)" ;
		push = con.prepareStatement(s) ;
    }
    
	

	public void run() throws ParserConfigurationException, SAXException, IOException {
		Path file = Paths.get(FileImporter.basePath.toString(),"data", "sql", "pagedescription.xml") ;
		
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(this);
	    xmlReader.parse(file.toString()) ;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		switch(localName){
		case "title" :
		case "abstract" :
			fillBuffer = true ;
			break ;
			
		default :
			break ;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		switch(localName){
		case "title" :
			fillBuffer = false ;
			title = buff.toString() ;
			buff.setLength(0) ;
			break ;
		case "abstract" :
			fillBuffer = false ;
			abstr = buff.toString() ;
			buff.setLength(0) ;
			try {
				sendQuery(title,abstr) ;
			} catch (SQLException e) {
				e.printStackTrace();
				throw new SAXException() ;
			}
			break ;
		default : 
			break ;
		}
		
	}
	
	private void sendQuery(String title, String abstr) throws SQLException {
		title =title.substring(12) ;
		nb_pending ++ ;
		push.setString(2*nb_pending -1, title) ;
		push.setString(2* nb_pending, abstr) ;
		
		if(nb_pending == nb_sent)
		{
			
			push.executeUpdate() ;
			push.clearParameters() ;
			nb_pending = 0 ; 
			nb_lines ++ ;
			System.out.println(1000* nb_lines) ;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if(fillBuffer)
			buff.append(ch, start, length) ;
	}
}
