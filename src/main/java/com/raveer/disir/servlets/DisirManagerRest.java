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

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raveer.disir.singletons.PropertiesContainer;

@WebServlet( name="DisirManagerRestServlet", displayName="Disir Manager Rest Servlet", urlPatterns = {"/disir/rest/*"}, loadOnStartup=1)
public class DisirManagerRest extends HttpServlet {
	private static final long serialVersionUID = 15L;
	
	private static final String GET_DATASOURCES = "datasources";
	private static final String READ_KEY_VALUE = "read";
	private static final String UPDATE_KEY_VALUE = "update";
	
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
		if (uriTokens.length>4) {
			switch (uriTokens[4]) {
				case GET_DATASOURCES:
					data = getDataSourcesJson();
					break;
				case READ_KEY_VALUE:
					data = readKeyValueJson(uri);
					break;
				case UPDATE_KEY_VALUE:
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
	
	private String readKeyValueJson(String uri) {
		ObjectMapper mapper = new ObjectMapper();
		String data;
		try {
			data = mapper.writeValueAsString(readKeyValue(uri));
		} catch (JsonProcessingException e) {
			data = "{\"error\":\"JSON Transform failed gettind key/value pairs: - " + e.getMessage() +"\"}";
			e.printStackTrace();
		}
		return data;
	}
	
	private HashMap<String, Object> readKeyValue(String uri) {
		HashMap<String, Object> data = new HashMap<String, Object>();		
		String uriTokens[] = uri.split("/");
		
		if (uriTokens.length>5) {
			String datasourceName = null;
			try {
				InitialContext ctx = new InitialContext();
				NamingEnumeration<NameClassPair> list = ctx.list("java:comp/env/jdbc");
				while (list.hasMore()) {
					if (list.next().getName().equals(uriTokens[5])) {
						datasourceName = uriTokens[5];
						break;
					}
				}
				if (datasourceName!=null) {
					try {
						DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/"+datasourceName);
						try (Connection connection = dataSource.getConnection()) {
							String sql = "SELECT * FROM disir_properties";
							try(Statement stmt = connection.createStatement()) {
								List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
								try(ResultSet rs = stmt.executeQuery(sql)) {
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
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
						e.printStackTrace();
					}
				} else {
					data.put("error", "Unknown Datasource: " + uriTokens[5]);
				}
			} catch (NamingException e) {
				data.put("error", "Error Finding JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
				e.printStackTrace();
			}
		} else {
			data.put("error", "Missing Parameter - DataSource Name");
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
		
		/*
		for (Entry<String, String[]> param : params.entrySet()) {
			for (String value : param.getValue()) {
				System.out.println(param.getKey() + ":" + value);
			}
		}
		*/
		
		if (uriTokens.length==6 && action!=null) {
			if (action.equals("addRow")) {
				addRow(data, uriTokens, restNameSpace, restKey, restValue);
			} else if (action.equals("editRow")) {
				updateRow(data,uriTokens,restId,restNameSpace,restKey,restValue);
			} else if (action.equals("deleteRow")) {
				deleteRow(data, uriTokens, restId);
			} else {
				data.put("error", "Unknown action: " + action);
			}
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
	
	private void addRow(HashMap<String, Object> data, String[] uriTokens, String[] restNameSpace, String[] restKey, String[] restValue) {
		boolean validData = false;
		if (restNameSpace!=null && restNameSpace.length==1) {
			if (restKey!=null && restKey.length==1) {
				if (restValue!=null && restValue.length==1) {
					validData=true;
				}
			}
		}
		
		if (validData) {
			String datasourceName = null;
			String nameSpace=restNameSpace[0];
			String key=restKey[0];
			String value=restValue[0];
					
			try {
				InitialContext ctx = new InitialContext();
				NamingEnumeration<NameClassPair> list = ctx.list("java:comp/env/jdbc");
				while (list.hasMore()) {
					if (list.next().getName().equals(uriTokens[5])) {
						datasourceName = uriTokens[5];
						break;
					}
				}
				if (datasourceName!=null) {
					try {
						boolean uniqueRow = true;
						DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/"+datasourceName);
						try (Connection connection = dataSource.getConnection()) {
							String sql = "SELECT * from disir_properties WHERE namespace = ? AND key = ?";
							try(PreparedStatement stmt = connection.prepareStatement(sql)) {
								stmt.setString(1, nameSpace);
								stmt.setString(2, key);
								try (ResultSet rs = stmt.executeQuery()) {
									if (rs.next()) {
										uniqueRow = false;
										data.put("error","Duplicate NameSpace/Key value: " + rs.getString("value"));
									}
								}
							}
							if (uniqueRow) {
							sql = "INSERT INTO disir_properties (NAMESPACE,KEY,VALUE) VALUES (?,?,?)";
								try(PreparedStatement stmt = connection.prepareStatement(sql)) {
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
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
						e.printStackTrace();
					}
				} else {
					data.put("error", "Unknown Datasource: " + uriTokens[5]);
				}
			} catch (NamingException e) {
				data.put("error", "Error Finding JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void deleteRow(HashMap<String, Object> data, String[] uriTokens, String[] id) {
		if (id!=null && id.length==1) {
			int rowId=Integer.parseInt(id[0]);
			String datasourceName = null;
			try {
				InitialContext ctx = new InitialContext();
				NamingEnumeration<NameClassPair> list = ctx.list("java:comp/env/jdbc");
				while (list.hasMore()) {
					if (list.next().getName().equals(uriTokens[5])) {
						datasourceName = uriTokens[5];
						break;
					}
				}
				if (datasourceName!=null) {
					try {
						DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/"+datasourceName);
						try (Connection connection = dataSource.getConnection()) {
							String sql = "DELETE FROM disir_properties WHERE id = ?";
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
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
						e.printStackTrace();
					}
				} else {
					data.put("error", "Unknown Datasource: " + uriTokens[5]);
				}
			} catch (NamingException e) {
				data.put("error", "Error Finding JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
				e.printStackTrace();
			}
		} else {
			data.put("error", "Missing Parameter - Row ID");
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
	
	private HashMap<String, Object> getDataSources() {
		//TODO parse Annotations for used datasources/tables/sql
		HashMap<String, Object> data = new HashMap<String,Object>();
		List<String> dataSources = new ArrayList<String>();
		try {
			InitialContext ctx = new InitialContext();
			NamingEnumeration<NameClassPair> list = ctx.list("java:comp/env/jdbc");
			while (list.hasMore()) {
				dataSources.add(list.next().getName());
			}
			ctx.close();
			data.put("datasources", dataSources);
		} catch (NamingException e) {
			data.put("error", "Error finding JNDI JDBC: "+ e.getMessage());
		}
		
		return data;
	}

	private void updateRow(HashMap<String, Object> data, String[] uriTokens, String[] restId, String[] restNameSpace, String[] restKey, String[] restValue) {
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
			String datasourceName = null;
			Integer id=Integer.parseInt(restId[0]);
			String nameSpace=restNameSpace[0];
			String key=restKey[0];
			String value=restValue[0];
					
			try {
				InitialContext ctx = new InitialContext();
				NamingEnumeration<NameClassPair> list = ctx.list("java:comp/env/jdbc");
				while (list.hasMore()) {
					if (list.next().getName().equals(uriTokens[5])) {
						datasourceName = uriTokens[5];
						break;
					}
				}
				if (datasourceName!=null) {
					try {
						DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/"+datasourceName);
						try (Connection connection = dataSource.getConnection()) {
							String sql = "UPDATE disir_properties SET namespace = ?, key=?, value = ? WHERE id = ?";
							try(PreparedStatement stmt = connection.prepareStatement(sql)) {
								stmt.setString(1, nameSpace);
								stmt.setString(2, key);
								stmt.setString(3, value);
								stmt.setInt(4, id);
								int rowsUpdated = stmt.executeUpdate();
								
								data.put("success",rowsUpdated);
							}
						} catch (SQLException e) {
							data.put("error", "Error SQL: "+ e.getMessage());
							e.printStackTrace();
						}
					} catch (NamingException e) {
						data.put("error", "Error Getting DataSource from JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
						e.printStackTrace();
					}
				} else {
					data.put("error", "Unknown Datasource: " + uriTokens[5]);
				}
			} catch (NamingException e) {
				data.put("error", "Error Finding JNDI JDBC Name "+ datasourceName + ": "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
