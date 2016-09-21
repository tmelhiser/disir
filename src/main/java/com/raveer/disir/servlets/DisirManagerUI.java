package com.raveer.disir.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raveer.disir.utils.StringUtils;

@WebServlet( name="DisirManagerUIServlet", displayName="Disir Manager UI Servlet", urlPatterns = {"/disir/ui"}, loadOnStartup=10)
public class DisirManagerUI extends HttpServlet {
	private static final long serialVersionUID = 15L;
	private static final Logger LOGGER = Logger.getLogger(DisirManagerUI.class.getName());
	private static final String UI_FILE = "/com/raveer/disir/resources/index.html";
	public static String INDEX = null;
	
	static {
		try (InputStream is = DisirManagerUI.class.getResourceAsStream(UI_FILE)) {
			StringBuilder textBuilder = new StringBuilder();
		    try (Reader reader = new BufferedReader(new InputStreamReader
		      (is, Charset.forName(StandardCharsets.UTF_8.name())))) {
		        int c = 0;
		        while ((c = reader.read()) != -1) {
		            textBuilder.append((char) c);
		        }
		    }
		    INDEX = textBuilder.toString();
		} catch (Exception e) {
			
		}

	    List<String> javascriptFiles = getResourceFiles("/com/raveer/disir/resources/javascript");
	    List<String> cssFiles = getResourceFiles("/com/raveer/disir/resources/css");
	    
	    INDEX = INDEX.replace("%%%JAVA_SCRIPT%%%",insertResourceLink(javascriptFiles,"<script src='resources/%s\'></script>"));
	    INDEX = INDEX.replace("%%%STYLE_SHEET%%%", insertResourceLink(cssFiles,"<link rel='stylesheet' href='resources/%s\'></script>"));
	    LOGGER.info("Added Dynamic Resources to Index Page");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		resp.getOutputStream().print(INDEX);
	}
	
	private static String insertResourceLink(List<String> resourceFiles, String string) {
		StringBuilder sb = new StringBuilder();
		for (String resource : resourceFiles) {
			sb.append(String.format(string, resource));
		}
		return sb.toString();
	}
	
	private static List<String> getResourceFiles(String path) {
		List<String> fileList = new ArrayList<String>();
		try (InputStream is = DisirManagerUI.class.getResourceAsStream(path)) {
			if (is != null) {
				String type = StringUtils.last(path.split("/"));
				try(InputStreamReader isr = new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name()))) {
			    	try (BufferedReader reader = new BufferedReader(isr)) {
			    		String resource;
			    		while( (resource = reader.readLine()) != null ) {
			    			fileList.add( type + "/" + resource );
			    		}
			    	} catch (IOException ioe) {
			    		System.out.println("BufferedReader");
			    		ioe.printStackTrace();
			    	}
				} catch(IOException ioe) {
					System.out.println("InputStreamReader");
					ioe.printStackTrace();
				}
			}
	    } catch (IOException ioe) {
	    	System.out.println("InputStream");
	    	ioe.printStackTrace();
	    }
		for (String file : fileList) {
			LOGGER.info("Resource File Found: " + file);
		}
		return fileList;
	}
}
