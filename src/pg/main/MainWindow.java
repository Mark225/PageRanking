package pg.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jgrapht.DirectedGraph;

import pg.algo.Edge;
import pg.algo.Node;
import pg.algo.Salsa;
import pg.graphimport.GraphImport;
import pg.graphimport.GraphImport.Mode;

public class MainWindow extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JLabel modeLabel = new JLabel("Mode") ;
	private JComboBox<GraphImport.Mode> modeBox = new JComboBox<GraphImport.Mode>(GraphImport.Mode.class.getEnumConstants());
	private JPanel modePan = new JPanel() ;
	
	//---citeseer import
	private JLabel searchLabel = new JLabel("Search") ;
	private JTextField searchField = new JTextField();
	private JPanel searchPan = new JPanel() ;
	//---
	
	//---file import
	private JPanel fileSelectPan = new JPanel() ;
	private JComboBox<String> fileBox = new JComboBox<String>(GraphImport.availableFiles()) ;
	private JLabel fileSelectLabel = new JLabel("Select File :") ;
	//---
	
	//---buttonsPan
	private JPanel buttonsPan = new JPanel() ;
	private JButton searchButton = new JButton("Search") ;
	private JButton cancelButllon = new JButton("Cancel") ;
	//---
	
	private JPanel separator = new JPanel() ;
	
	//---resultsPan
	private JPanel resultsPan = new JPanel() ;
	private JTextArea resultsField = new JTextArea(30,70) ;
	private JScrollPane resultsScroll = new JScrollPane(resultsField) ;
	//---
	
	private boolean isSearching = false ;
	private Thread runningThread = null ;
	
	public MainWindow(){
		super("Page Ranking algorithm") ;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600) ;
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		//--modePan
		modeBox.addActionListener(this) ;
		modeBox.setMaximumSize(new Dimension(1000,20)) ;
		modePan.setLayout(new BoxLayout(modePan, BoxLayout.X_AXIS)) ;
		modePan.setMaximumSize(new Dimension(1000, 40)) ;
		modePan.setPreferredSize(new Dimension(800, 30)) ;
		modePan.add(modeLabel) ;
		modePan.add(Box.createRigidArea(new Dimension(10, 0))) ;
		modePan.add(modeBox) ;
		modePan.add(Box.createHorizontalGlue()) ;
		getContentPane().add(modePan) ;
		
		//---searchPan
		searchPan.setLayout(new BoxLayout(searchPan, BoxLayout.X_AXIS)) ;
		searchPan.setLayout(new BoxLayout(searchPan, BoxLayout.X_AXIS)) ;
		searchField.setMaximumSize(new Dimension(1000, 20));
		searchField.setPreferredSize(new Dimension(400, 20)) ;
		searchPan.add(searchLabel) ;
		searchPan.add(Box.createRigidArea(new Dimension(10, 0))) ;
		searchPan.add(searchField) ;
		searchPan.add(Box.createHorizontalGlue()) ;
		getContentPane().add(searchPan) ;
		//---
		
		//---buttonPan
		buttonsPan.setLayout(new BoxLayout(buttonsPan, BoxLayout.X_AXIS)) ;
		searchButton.addActionListener(this) ;
		buttonsPan.add(searchButton) ;
		cancelButllon.addActionListener(this) ;
		buttonsPan.add(cancelButllon) ;
		getContentPane().add(buttonsPan) ;
		//---
		
		//---fileSelectPan
		fileSelectPan.setLayout(new BoxLayout(fileSelectPan, BoxLayout.Y_AXIS)) ;
		fileSelectPan.setMaximumSize(new Dimension(1000, 40)) ;
		fileSelectPan.setPreferredSize(new Dimension(800, 30)) ;
		fileSelectPan.setLayout(new BoxLayout(fileSelectPan, BoxLayout.X_AXIS)) ;
		fileSelectPan.add(fileSelectLabel) ;
		fileSelectPan.add(Box.createRigidArea(new Dimension(5,0))) ;
		fileBox.setMaximumSize(new Dimension(1000, 20)) ;
		fileSelectPan.add(fileBox) ;
		fileSelectPan.add(Box.createHorizontalGlue()) ;
		//---
		
		//---separator
		separator.add(Box.createVerticalStrut(5)) ;
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL) ;
		sep.setPreferredSize(new Dimension(1000,10)) ;
		separator.add(sep) ;
		separator.add(Box.createVerticalStrut(5)) ;
		getContentPane().add(separator) ;
		//---
		
		//---resultPan
		resultsPan.add(resultsScroll) ;
		resultsField.setEditable(false) ;
		getContentPane().add(resultsPan) ;
		//---
		
		getContentPane().add(Box.createVerticalGlue()) ;
		
		setVisible(true) ;
		
	}

	public void displayResults(final Collection<String> results){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				resultsField.setText("") ;
				for(String result : results)
				{
					resultsField.append(result  + "\n------------------------------------------------------------------\n") ;
				}
				
			}
		}) ;

	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == modeBox && !isSearching)
		{
			getContentPane().removeAll() ; 
			getContentPane().add(modePan) ;
			GraphImport.Mode mode = modeBox.getItemAt(modeBox.getSelectedIndex()) ;
			
			switch (mode) {
			case CITESEERX:
				getContentPane().add(searchPan) ;
				getContentPane().add(buttonsPan) ;
				getContentPane().add(separator) ;
				getContentPane().add(resultsPan) ;
				break;
				
			case FROM_FILE:
				getContentPane().add(fileSelectPan) ;
				getContentPane().add(buttonsPan) ;
				getContentPane().add(separator) ;
				getContentPane().add(resultsPan) ;
				break ;
			}
			getContentPane().revalidate() ;
			getContentPane().repaint() ;
		}
		if(event.getSource() == searchButton && !isSearching)
		{
			switch (modeBox.getItemAt(modeBox.getSelectedIndex())) {
			case CITESEERX:
				String search = searchField.getText() ;
				//Replace ' ' by '+'
				StringBuilder builder = new StringBuilder(search) ;
				int i = builder.lastIndexOf(" ") ;
				while(i != -1)
				{
					builder.setCharAt(i, '+') ;
					i = builder.lastIndexOf(" ") ;
				}
				run(Mode.CITESEERX, builder.toString()) ;
				break;

			case FROM_FILE:
				run(Mode.FROM_FILE, fileBox.getItemAt(fileBox.getSelectedIndex())) ;
				break;
			}
		}
		if(event.getSource() == cancelButllon && isSearching)
		{
			System.out.println("Interrupting") ;
			runningThread.stop() ;
			isSearching = false ;
		}
		
	}
	
	/**
	 * Runs the algorithm with the given parameters.
	 * Starts a new thread for the task
	 * @param mode
	 * @param search
	 */
	public void run(final GraphImport.Mode mode, final String search){
		isSearching = true ;
		runningThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DirectedGraph<Node, Edge> g = 
							GraphImport.importFrom(mode,search) ;
					Map<Node, Double> result = Salsa.apply(g) ;
					List<Map.Entry<Node,Double>> res_list = new LinkedList<>(result.entrySet()) ;
					Collections.sort(res_list, new ResultComparator()) ;
					
					Vector<String> string_res = new Vector<String>() ;
					for(Map.Entry<Node, Double> res : res_list)
					{
						string_res.add(res.getKey().getName() + "\n" + 
								res.getKey().description+ "\nScore : " + res.getValue()) ;
					}
					
					displayResults(string_res) ;
					
					isSearching = false ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}) ;
		runningThread.start() ;
	}
	
	class ResultComparator implements Comparator<Map.Entry<Node, Double>>{

		@Override
		public int compare(Entry<Node, Double> arg0, Entry<Node, Double> arg1) {
			double d = arg0.getValue() - arg1.getValue() ;
			int result = 0;
			if(d < 0f)
				result = 1 ;
			if(d > 0f)
				result = -1 ;
			return result;
		}
		
		
		
	}
	

}
