package com.raveer.disir.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raveer.disir.utils.StringUtils;

@WebServlet( name="DisirManagerResourcesServlet", displayName="Disir Manager Resources Servlet", urlPatterns = {"/disir/resources/*"}, loadOnStartup=100)
public class DisirManagerResources extends HttpServlet {
	private static final long serialVersionUID = 15L;
	private static Map<String,String> javascriptResources = new HashMap<String,String>();
	private static Map<String,String> cssResources = new HashMap<String,String>();
	private static Map<String,byte[]> imageResources = new HashMap<String,byte[]>();
	private static Map<String,byte[]> fontResources = new HashMap<String,byte[]>();
	
	static {
		for (String path : Arrays.asList(new String[] {"/com/raveer/disir/resources/javascript","/com/raveer/disir/resources/css","/com/raveer/disir/resources/images", "/com/raveer/disir/resources/fonts"})) {
			try (InputStream is = DisirManagerResources.class.getResourceAsStream(path)) {
				String type = StringUtils.last(path.split("/"));
		    	try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {
		    		String resource;
		    		while( (resource = reader.readLine()) != null ) {
		    			try (InputStream is1 = DisirManagerUI.class.getResourceAsStream(path + "/" + resource)) {
		    				if (type.equals("javascript") || type.equals("css")) {
			    				StringBuilder textBuilder = new StringBuilder();
			    			    try (Reader reader1 = new BufferedReader(new InputStreamReader
			    			      (is1, Charset.forName(StandardCharsets.UTF_8.name())))) {
			    			        int c = 0;
			    			        while ((c = reader1.read()) != -1) {
			    			            textBuilder.append((char) c);
			    			        }
			    			    }
			    			    if (type.equals("javascript")) {
			    			    	javascriptResources.put(resource,textBuilder.toString());
			    			    } else if (type.equals("css")) {
			    			    	cssResources.put(resource,textBuilder.toString());
			    			    } else {
			    			    	System.out.println("Don't know what to do with this resource yet: " + type +"/" +resource);
			    			    }
		    				} else {
		    					ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		    					int nRead;
		    					byte[] data = new byte[16384];

		    					while ((nRead = is1.read(data, 0, data.length)) != -1) {
		    					  buffer.write(data, 0, nRead);
		    					}

		    					buffer.flush();

		    					if (type.equals("images")) {
		    						imageResources.put(resource, buffer.toByteArray());
		    					} else if (type.equals("fonts")) {
		    						fontResources.put(resource, buffer.toByteArray());
		    					} else {
		    						System.out.println("Don't know what to do with this resource yet: " + type +"/" +resource);
		    					}
		    					
		    				}
		    			} catch (Exception e) {
		    				
		    			}
		    		}
		    	}
		    } catch (Exception e) {
		    	
		    }
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri[] = req.getRequestURI().split("/");
		if (uri.length==6) {
			
			String contentType="application/javascript";
			String data = javascriptResources.get(uri[5]);
			boolean gotData = (data != null);
			
			if (gotData) {
				resp.setContentType(contentType);
				resp.getWriter().print(data);
				return;
			}
			
			contentType="text/css";
			data = cssResources.get(uri[5]);
			gotData = (data != null);
			
			if (gotData) {
				resp.setContentType(contentType);
				resp.getWriter().print(data);
				return;
			}
			
			
			contentType="image/png";
			byte byteData[] = imageResources.get(uri[5]);
			gotData = (byteData != null);
			
			if (gotData) {
				resp.setContentType(contentType);
				resp.getOutputStream().write(byteData);
				return;
			}
			
			byteData = fontResources.get(uri[5]);
			gotData = (byteData != null);
			if (gotData) {
				if (uri[5].endsWith(".eot")) {
					contentType="application/x-opentype";
				} else if (uri[5].endsWith(".svg")) {
					contentType="image/svg+xml";
				} else if (uri[5].endsWith(".ttf")) {
					contentType="application/x-font-ttf";
				} else if (uri[5].endsWith(".woff")) {
					contentType="application/font-woff";
				} else if (uri[5].endsWith(".woff2")) {
					contentType="application/font-woff";
				} else {
					contentType="application/x-opentype";
				}
				resp.setContentType(contentType);
				resp.getOutputStream().write(byteData);
				return;
			}
			
			super.doGet(req, resp);
		} else {
			super.doGet(req, resp);
		}
	}

}
