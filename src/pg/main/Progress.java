package pg.main;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Progress {
	
	private JPanel separator = new JPanel() ;
	private JTextArea textArea = new JTextArea(5,70);
	private JScrollPane scrollPane = new JScrollPane(textArea);
	private JProgressBar progBar = new JProgressBar(0, 1000);
	
	private JPanel pan = new JPanel();
	
	
	public Progress() {
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS)) ;
		pan.setPreferredSize(new Dimension(1000,150)) ;
		
		//---separator
		separator.add(Box.createVerticalStrut(5)) ;
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL) ;
		sep.setPreferredSize(new Dimension(1000,10)) ;
		separator.add(sep) ;
		separator.add(Box.createVerticalStrut(5)) ;
		pan.add(separator) ;
		//---	
		
		//---textArea
		pan.add(scrollPane) ;
		textArea.setEditable(false) ;
		//---
		
		//---progressBar
		progBar.setStringPainted(true) ;
		progBar.setValue(0) ;
		progBar.setMinimumSize(new Dimension(100, 10)) ;
		pan.add(progBar) ;
		
		
	}
	
	
	public JPanel progressPanel(){
		return pan ;
	}
	
	/**
	 * Set the progress of the progress bar
	 * @param i an integer between 0 and 1 000. 
	 */
	public void setProgress(final int i){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progBar.setValue(i) ;				
			}
		}) ;
	}
	
	/**
	 * Sets the value of the progress bar, with max the maximum value.
	 * @param value
	 * @param max
	 */
	public void setProgress(int value, int max) {
		setProgress(value*1000 / max) ;
	}
	
	/**
	 * Displays an INFO message in the dialog message
	 * @param msg
	 */
	public void info(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textArea.append("[INFO] " + msg + "\n") ;
			}
		}) ;
	}
	
	/**
	 * Displays a warning in the dialog message
	 * @param msg
	 */
	public void warning(final String msg){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textArea.append("[WARNING] " + msg + "\n") ;				
			}
		}) ;		
	}
	
	/**
	 * Displays an error in the dialog message
	 * @param msg
	 */
	public void error(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textArea.append("[ERROR] " + msg + "\n") ;				
			}
		}) ;		
	}

}
