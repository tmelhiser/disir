package com.raveer.disir.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raveer.disir.beans.PropertyManager;
import com.raveer.disir.listeners.ScanAnnotations;
import com.raveer.disir.singletons.PropertiesContainer;

@WebServlet( name="DisirManagerRestServlet", displayName="Disir Manager Rest Servlet", urlPatterns = {"/disir/rest/*"}, loadOnStartup=50)
public class DisirManagerRest extends HttpServlet {
	private static final long serialVersionUID = 15L;
	private static final Logger LOGGER = Logger.getLogger(DisirManagerRest.class.getName());
	
	private static final String GET_DATASOURCES = "datasources";
	private static final String DO_ACTION = "action";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String data = processAction(req.getRequestURI(),req);
		resp.setContentType("application/json");
		resp.getOutputStream().print(data);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String data = processAction(req.getRequestURI(), req);
		resp.setContentType("application/json");
		resp.getOutputStream().print(data);
	}
	
	private String processAction(String uri,HttpServletRequest req) {
		String uriTokens[] = uri.split("/");
		String data = null;
		if (uriTokens.length==5) {
			switch (uriTokens[4]) {
				case GET_DATASOURCES:
					data = getDataSourcesJson();
					break;
				case DO_ACTION:
					data = processRestData(uri,req);
					break;
				default:
					data = "{\"error\":\"Unknown Action - " + uriTokens[4] + "\"}";
			}
		}
		if (data == null) {
			data = "{\"version:\"" + PropertiesContainer.VERSION +"\"}";
		}
		
		return data;
	}
	
	private String processRestData(String uri, HttpServletRequest req) {
		HashMap<String, Object> data = new HashMap<String, Object>();		
		String uriTokens[] = uri.split("/");
		Map<String,String[]> params = req.getParameterMap();
		String[] restId = params.get("raw[data][0][id]");
		String[] restNameSpace = params.get("raw[data][0][nameSpace]");
		String[] restKey = params.get("raw[data][0][key]");
		String[] restValue = params.get("raw[data][0][value]");
		String action = params.get("action") != null ? params.get("action")[0] : null;
		
		if (LOGGER.isLoggable(Level.FINE)) {
			for (Entry<String, String[]> param : params.entrySet()) {
				for (String value : param.getValue()) {
					System.out.println(param.getKey() + ":" + value);
				}
			}
		}
		
		if (uriTokens.length==5 && action!=null) {
			if (action.equals("readRows")) {
				getAllRows(data,req);
			} else if (action.equals("addRow")) {
				addRow(data, req, restNameSpace, restKey, restValue);
			} else if (action.equals("editRow")) {
				updateRow(data,req,restId,restNameSpace,restKey,restValue);
			} else if (action.equals("deleteRow")) {
				deleteRow(data, req, restId);
			} else {
				data.put("error", "Unknown action: " + action);
			}
		} else {
			data.put("error", "Malformed URI - " + uri);
		}
		
		String dataJson;
		try {
			ObjectMapper mapper = new ObjectMapper();
			dataJson = mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			dataJson = "{\"error\":\"JSON Transform failed gettind key/value pairs: - " + e.getMessage() +"\"}";
			e.printStackTrace();
		}
		return dataJson;
	}
	
	private void getAllRows(HashMap<String, Object> data, HttpServletRequest req) {
		PropertyManager pm = new PropertyManager();
		
		//TODO match jndiDBName/dbTableName from Post with Scanned Area and look up SQL
		String jndiDBName = req.getParameter("jndiDBName");
		String dbTableName = req.getParameter("dbTableName");
		
		jndiDBName = (jndiDBName==null || jndiDBName.trim().equals(""))  ? pm.getDEFAULT_JNDI_DB_NAME() : req.getParameter("jndiDBName");
		dbTableName = (dbTableName==null || dbTableName.trim().equals(""))  ? pm.getDEFAULT_DB_TABLE_NAME() : req.getParameter("dbTableName");
		
		String sqlSelect = String.format(pm.getDEFAULT_SQL_SELECT(), dbTableName);
		
		InitialContext ctx = null;
		try {
			ctx =  new InitialContext();
			DataSource dataSource = (DataSource) ctx.lookup(jndiDBName);
			try (Connection connection = dataSource.getConnection()) {
				try(Statement stmt = connection.createStatement()) {
					List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
					try(ResultSet rs = stmt.executeQuery(sqlSelect)) {
						ResultSetMetaData rsmd = rs.getMetaData();
						List<String> columnNames = new ArrayList<String>();
						for(int count=1;count<=rsmd.getColumnCount();count++) {
							columnNames.add(rsmd.getColumnName(count));
						}	
						while(rs.next()) {
							Map<String, Object> row = new HashMap<String, Object>();
							for (String column : columnNames) {
								row.put(column, rs.getObject(column));
							}
							results.add(row);
						}	
					}
					data.put("data",results);
				}
			} catch (SQLException e) {
				data.put("error", "Error SQL: "+ e.getMessage());
				e.printStackTrace();
			}
		} catch (NamingException e) {
			data.put("error", "Error Getting DataSource from JNDI JDBC Name("+ jndiDBName + "): "+ e.getMessage());
			e.printStackTrace();
		} finally {
			if (ctx!=null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					LOGGER.fine("Error Closing InitialContext: " + e.getMessage());
				}
			}
		}
	}
	
	private void addRow(HashMap<String, Object> data, HttpServletRequest req, String[] restNameSpace, String[] restKey, String[] restValue) {
		
		//TODO match jndiDBName/dbTableName from Post with Scanned Area and look up SQL
		boolean validData = false;
		if (restNameSpace!=null && restNameSpace.length==1) {
			if (restKey!=null && restKey.length==1) {
				if (restValue!=null && restValue.length==1) {
					validData=true;
				}
			}
		}
		
		if (validData) {
			PropertyManager pm = new PropertyManager();
			String nameSpace=restNameSpace[0];
			String key=restKey[0];
			String value=restValue[0];
			
			String jndiDBName = req.getParameter("jndiDBName");
			String dbTableName = req.getParameter("dbTableName");
			
			jndiDBName = (jndiDBName==null || jndiDBName.trim().equals(""))  ? pm.getDEFAULT_JNDI_DB_NAME() : req.getParameter("jndiDBName");
			dbTableName = (dbTableName==null || dbTableName.trim().equals(""))  ? pm.getDEFAULT_DB_TABLE_NAME() : req.getParameter("dbTableName");
			
			String sqlUnique = String.format(pm.getDEFAULT_SQL_ROW(), dbTableName);
			String sqlInsert = String.format(pm.getDEFAULT_SQL_INSERT(), dbTableName);
			
			InitialContext ctx = null;
					
			
					try {
						ctx = new InitialContext();
						DataSource dataSource = (DataSource) ctx.lookup(jndiDBName);
						boolean uniqueRow = true;
						try (Connection connection = dataSource.getConnection()) {
							try(PreparedStatement stmt = connection.prepareStatement(sqlUnique)) {
								stmt.setString(1, nameSpace);
								stmt.setString(2, key);
								try (ResultSet rs = stmt.executeQuery()) {
									if (rs.next()) {
										uniqueRow = false;
										data.put("error","Existing NameSpace+Key Pair - Current Value: " + rs.getString("value"));
									}
								}
							}
							if (uniqueRow) {
								try(PreparedStatement stmt = connection.prepareStatement(sqlInsert)) {
									stmt.setString(1, nameSpace);
									stmt.setString(2, key);
									stmt.setString(3, value);
									int rowsInserted = stmt.executeUpdate();
									
									data.put("success",rowsInserted);
								}
							}
						} catch (SQLException e) {
							data.put("error", "Error SQL: "+ e.getMessage());
							e.printStackTrace();
						}
					} catch (NamingException e) {
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ jndiDBName + ": "+ e.getMessage());
						e.printStackTrace();
					} finally {
						if (ctx != null) {
							try {
								ctx.close();
							} catch (NamingException e) {
								LOGGER.fine("Error Closing InitialContext: " + e.getMessage());
							}
						}
					}
				
		}
	}

	private void deleteRow(HashMap<String, Object> data, HttpServletRequest req, String[]  id) {
		//TODO match jndiDBName/dbTableName from Post with Scanned Area and look up SQL
		if (id!=null && id.length==1) {
			PropertyManager pm = new PropertyManager();
			int rowId=Integer.parseInt(id[0]);
			
			String jndiDBName = req.getParameter("jndiDBName");
			String dbTableName = req.getParameter("dbTableName");
			
			jndiDBName = (jndiDBName==null || jndiDBName.trim().equals(""))  ? pm.getDEFAULT_JNDI_DB_NAME() : req.getParameter("jndiDBName");
			dbTableName = (dbTableName==null || dbTableName.trim().equals(""))  ? pm.getDEFAULT_DB_TABLE_NAME() : req.getParameter("dbTableName");
			
			String sql = String.format(pm.getDEFAULT_SQL_DELETE(), dbTableName);
			
			InitialContext ctx = null;
				
			try {
				ctx = new InitialContext();
				DataSource dataSource = (DataSource) ctx.lookup(jndiDBName);
				try (Connection connection = dataSource.getConnection()) {
					try(PreparedStatement stmt = connection.prepareStatement(sql)) {
						stmt.setInt(1, rowId);
						int rowsDeleted = stmt.executeUpdate();
						
						data.put("success",rowsDeleted);
					}
				} catch (SQLException e) {
					data.put("error", "Error SQL: "+ e.getMessage());
					e.printStackTrace();
				}
			} catch (NamingException e) {
				data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ jndiDBName + ": "+ e.getMessage());
				e.printStackTrace();
			} finally {
				if (ctx != null) {
					try {
						ctx.close();
					} catch (NamingException e) {
						LOGGER.fine("Error Closing InitialContext: " + e.getMessage());
					}
				}
			}
		}
	}

	private String getDataSourcesJson() {
		ObjectMapper mapper = new ObjectMapper();
		String data;
		try {
			data = mapper.writeValueAsString(getDataSources());
		} catch (JsonProcessingException e) {
			data = "{\"error\":\"JSON Transform failed gettind datasource names: - " + e.getMessage() +"\"}";
			e.printStackTrace();
		}
		
		return data; 
	}
	
	private Map<String, Map<String, Map<String, String>>> getDataSources() {
		return ScanAnnotations.DATASOURCES;
	}

	private void updateRow(HashMap<String, Object> data, HttpServletRequest req, String[] restId, String[] restNameSpace, String[] restKey, String[] restValue) {
		//TODO match jndiDBName/dbTableName from Post with Scanned Area and look up SQL
		boolean validData = false;
		if (restId!=null && restId.length==1) {
			if (restNameSpace!=null && restNameSpace.length==1) {
				if (restKey!=null && restKey.length==1) {
					if (restValue!=null && restValue.length==1) {
						validData=true;
					}
				}
			}
		}
		
		if (validData) {
			PropertyManager pm = new PropertyManager();
			Integer id=Integer.parseInt(restId[0]);
			String nameSpace=restNameSpace[0];
			String key=restKey[0];
			String value=restValue[0];
			
			String jndiDBName = req.getParameter("jndiDBName");
			String dbTableName = req.getParameter("dbTableName");
			
			jndiDBName = (jndiDBName==null || jndiDBName.trim().equals(""))  ? pm.getDEFAULT_JNDI_DB_NAME() : req.getParameter("jndiDBName");
			dbTableName = (dbTableName==null || dbTableName.trim().equals(""))  ? pm.getDEFAULT_DB_TABLE_NAME() : req.getParameter("dbTableName");
			
			String sqlUnique = String.format(pm.getDEFAULT_SQL_ROW(), dbTableName);
			String sqlUpdate = String.format(pm.getDEFAULT_SQL_UPDATE(), dbTableName);
			
			InitialContext ctx = null;
					
					try {
						ctx = new InitialContext();
						DataSource dataSource = (DataSource) ctx.lookup(jndiDBName);
						boolean uniqueRow = true;
						try (Connection connection = dataSource.getConnection()) {
							try(PreparedStatement stmt = connection.prepareStatement(sqlUnique)) {
								stmt.setString(1, nameSpace);
								stmt.setString(2, key);
								try (ResultSet rs = stmt.executeQuery()) {
									while (rs.next()) {
										int rowId = rs.getInt("id");
										if (rowId != id) {
											uniqueRow = false;
											data.put("error","Existing NameSpace+Key Pair - Current Value: " + rs.getString("value"));
											break;
										}
									}
								}
							}
							if (uniqueRow) {
								try(PreparedStatement stmt = connection.prepareStatement(sqlUpdate)) {
									stmt.setString(1, nameSpace);
									stmt.setString(2, key);
									stmt.setString(3, value);
									stmt.setInt(4, id);
									int rowsUpdated = stmt.executeUpdate();
									
									data.put("success",rowsUpdated);
								}
							}
						} catch (SQLException e) {
							data.put("error", "Error SQL: "+ e.getMessage());
							e.printStackTrace();
						}
					} catch (NamingException e) {
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ jndiDBName + ": "+ e.getMessage());
						e.printStackTrace();
					} finally {
						if (ctx != null) {
							try {
								ctx.close();
							} catch (NamingException e) {
								LOGGER.fine("Error Closing InitialContext: " + e.getMessage());
							}
						}
					}

		}
	}

}
