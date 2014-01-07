package pg.algo;

import java.io.Serializable;

public class Node implements Serializable{
	private static final long serialVersionUID = 2L;
	/**
	 * Name associated to a node. In general, this name corresponds to an Internet Address.
	 * The name identifies uniquely the node.
	 */
	final String name ; 
	
	public String description = "" ;
	
	public Node(String name){
		this.name = name; 
	}
	
	public String getName(){
		return name ;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Node)
			return this.name.equals(((Node) obj).name) ;
		else 
			return false ;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode() ;
	}
}
