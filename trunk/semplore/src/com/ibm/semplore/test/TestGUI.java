/**
 * 
 */
package com.ibm.semplore.test;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.DebugIndex;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author xrsun
 *
 */
public class TestGUI extends JFrame{
	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	public static TermFactory termFactory = TermFactoryImpl.getInstance();
	private static IndexReaderImpl indexReader;
	
	private Toolkit toolkit;
    JTextField input;
    JTextArea area;
    JComboBox box;
    PrintStream out;
    
    private BoxAction[] actions = {
    		new BoxAction() {
				public void action(String input) {
					// TODO Auto-generated method stub
				}
				public String getName() {return "search instance by URI";}
    		},
    		new BoxAction() {
				public void action(String input) throws IOException, Exception {
					out.println(indexReader.getReader().document(Integer.valueOf(input)));
				}
				public String getName() {return "get document by docid";}
    		},
    		new BoxAction() {
				public void action(String input) throws IOException, Exception {
					DebugIndex.printDocStream(indexReader, indexReader.getDocStream(termFactory.createTermForRootCategories()));
				}
				public String getName() {return "search all categories";}
    		},
    		new BoxAction() {
				public void action(String input) throws IOException, Exception {
					DebugIndex.printDocStream(indexReader, indexReader.getDocStream(termFactory.createTermForRootRelations()));
				}
				public String getName() {return "search all relations";}
    		}
    }; 
    
	public TestGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(500, 400);
        toolkit = getToolkit();
        
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        getContentPane().add(panel);
        
        box = new JComboBox();
        for (BoxAction a: actions) {
        	box.addItem(a.getName());
        }
        panel.add(box);
    	
        input = new JTextField();
        panel.add(input);

    	JButton search = new JButton("Search");
     	panel.add(search);

     	area = new JTextArea();
     	JScrollPane spane = new JScrollPane(area);
     	panel.add(spane);
    	
    	search.addActionListener(new ActionListener() {
    	     public void actionPerformed(ActionEvent event) {
    	    	 area.setText("");
    	    	 try {
					actions[box.getSelectedIndex()].action(input.getText());
				} catch (Exception e) {
					e.printStackTrace(out);
				}
    	     }
    	 });
    	
		out = new PrintStream( new TextAreaOutputStream( area ) );
		System.setOut(out);
	}
	
	public static void main(String[] args) throws Exception {
		TestGUI g = new TestGUI();
		g.setVisible(true);
		
		Properties config = Config.readConfigFile(args[0]);
		XFacetedSearchService searchService = searchFactory
				.getXFacetedSearchService(config);
		XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		indexReader = (IndexReaderImpl)searcher.getInsIndexReader();
	}

}

interface BoxAction {
	public String getName();
	public void action(String input) throws Exception;
}

class TextAreaOutputStream extends OutputStream {
    private JTextArea textControl;
    
    /**
     * Creates a new instance of TextAreaOutputStream which writes
     * to the specified instance of javax.swing.JTextArea control.
     *
     * @param control   A reference to the javax.swing.JTextArea
     *                  control to which the output must be redirected
     *                  to.
     */
    public TextAreaOutputStream( JTextArea control ) {
        textControl = control;
    }
    
    /**
     * Writes the specified byte as a character to the
     * javax.swing.JTextArea.
     *
     * @param   b   The byte to be written as character to the
     *              JTextArea.
     */
    public void write( int b ) throws IOException {
        // append the data as characters to the JTextArea control
        textControl.append( String.valueOf( ( char )b ) );
    }  
}