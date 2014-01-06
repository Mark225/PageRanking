package pg.main;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ORG.oclc.oai.harvester2.verb.GetRecord;



public class Test {
	
	public static void test() throws IOException, ParserConfigurationException, SAXException, TransformerException{
		GetRecord ident = new GetRecord("http://citeseerx.ist.psu.edu/oai2","oai:CiteSeerX.psu:10.1.1.1.2596", "oai_dc") ;
		System.out.println(ident.toString()) ;
	}

}
