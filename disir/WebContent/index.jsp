<%@page import="com.raveer.disir.tests.Test1"%>
<%
long s = System.currentTimeMillis();
Test1 t1 = new Test1();
long e = System.currentTimeMillis();
%>
Hash Size: <%=t1.getKeyRingHash()%>
<br/>
Salt Size: <%=t1.getKeyRingSalt()%>
<br/>
Time To Run: <%=e-s%>