

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: UnisonServlet
 *
 */
 public class UnisonServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public UnisonServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("get " + request.getQueryString());
//		doPost(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("post " + request.getParameter("request"));
		System.out.println(request.getRemoteAddr());
		try {
			ForServlet fs = new ForServlet();
			
			InputStream inps = this.getServletContext().getResourceAsStream("/conf/path.cfg");
			BufferedReader rd = new BufferedReader(new InputStreamReader(inps));
			
			String path = rd.readLine();
			
			System.out.println("PATH = " + path);
			String configFile = path;
			fs.init(configFile);
			String queryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + request.getParameter("request");
			System.out.println("query\n" + queryXML);
//			String queryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
//			"<query>" +
//			"	<action type=\"all\" start=\"0\" count=\"10\"/>" +
//			"	<targetNode index=\"0\"/>" +
//			"	<graph>" +
//			"		<queryNode relation=\"\" conceptConstraint=\"&lt;http://dbpedia.org/class/yago/City108524735>\" keyword=\"earthquake\">" +
//			"		</queryNode>" +
//			"	</graph>" +
//			"</query>";
			String res = fs.query(queryXML, request.getRemoteAddr());
			
			System.out.println("begin query...");
			res = res.substring(38);
			response.getWriter().write(res);
			System.out.println("result \n" + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}   	  	    
}