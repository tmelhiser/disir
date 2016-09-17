# Disir

Annotation based properties manager with database, classpath file, and file system loading.

**What is Disir?** Disir is a multi-sourced property retrieval tool.  A databse backed key/value pair with a name space can be used to store and retrieve application property values.  It has the ability to fall back to traditional file based properties files.  The properties files can be located in the classpath or directly on the file system.  If the default file for the property exists, it takes precedence over the database backing; allowing real time overrides prior to database commits.

**Version 1.0.8 has been released**, with the ability to report which name space, classpath file, or file system based file the property was retrieved from; based on its coordinates. [(Release notes)](https://github.com/tmelhiser/disir/releases/tag/disir-1.0.1)

**What is a namespace** A namespace is a mechanism to keep multiple property groups separated by a prefix.  When housed in a database, the namespace allows multiple property groups to exist in the same table, without key collision.

**Disir is able to:**

* Retrieve grouped property key/value pairs from database with name spaces
* Option to retrieve properties from file hosted on file system or classpath
* Cached results for user configured period
* After caches expire, updated property values are available without restarts

**Property Source Order of Precedence** Disir can specify whether to prefer Database values or File based values first.  By default, file based values are prefered.

Load order when Files are prefered:
```
1. Files on file system
2. Files in classpath (first file matched in classpath)
3. Database backed values
```
Load order when Database is prefered:
```
1. Database backed values
2. Files on file system
3. Files in classpath (first file matched in classpath)
```
## Documentation
Disir configures its default behavior by reading the disir.properties file.  The name of the file and its location can be configured by passing a startup parameter to the JVM.
```
-Ddisir.defaults=/opt/default/disir.properties
```
The default values are:
```ini
default.nameSpace=APPLICATION
default.jndiDBName=java:comp/env/jdbc/DisirJDBC
default.dbTableName=disir_properties
default.sql=SELECT key,value FROM %s WHERE nameSpace = ?
default.propertiesFile=/app.properties
default.preferPropertiesFile=true
default.cache_expires=60000
```

####Annotation Example
```java
@PropertyManagerDefaults(jndiDBName = "java:comp/env/jdbc/DisirJDBC", nameSpace = "SSL", preferPropertiesFile = "false", propertiesFile = "/tmp/ssl.properties")
public class AnnotationDemo extends PropertyManagerBase {
	private static final long serialVersionUID = 1L;
	@PropertyManagerField(propertyKey = "keyring.hash", defaultValue = "SHA-256")
	String keyRingHash;
	@PropertyManagerField(propertyKey = "keyring.salt", defaultValue = "128")
	String keyRingSalt;

	public String getKeyRingHash() {
		return keyRingHash;
	}

	public String getKeyRingSalt() {
		return keyRingSalt;
	}

	public static void main(String args[]) {
		AnnotationDemo tests = new AnnotationDemo();
		System.out.println("Hash Size: " + tests.getKeyRingHash());
		System.out.println("Salt Size: " + tests.getKeyRingSalt());
	}
}
```
####Bean Example
```java
public class BeanDemo {
	private static final PropertyManager pm = new PropertyManager();
	static {
		pm.DEFAULT_JNDI_DB_NAME = "java:comp/env/jdbc/DisirJDBC";
		pm.DEFAULT_NAMESPACE = "SSL";
		pm.DEFAULT_PREFER_PROPERTIES_FILE = false;
		pm.DEFAULT_PROPERTIES_FILE = "/tmp/ssl.properties";
	}

	private String keyRingHash = pm.getProperty("keyring.hash", "SHA-1");
	private String keyRingSalt = pm.getProperty("keyring.salt", "32");

	public String getKeyRingHash() {
		return keyRingHash;
	}

	public String getKeyRingSalt() {
		return keyRingSalt;
	}

	public static void main(String args[]) {
		AnnotationDemo tests = new AnnotationDemo();
		System.out.println("Hash Size: " + tests.getKeyRingHash());
		System.out.println("Salt Size: " + tests.getKeyRingSalt());
	}
}
```
[See the wiki for full documentation.](https://github.com/tmelhiser/disir/wiki)

## Downloading

Use the "Clone or download" button at the top right of this page to get the source. You can get a pre-built JAR (usable in JRE 1.8 or later) from [Sonatype](https://oss.sonatype.org/#nexus-search;quick~disir), or add the following Maven Central dependency:

```xml
<dependency>
    <groupId>com.raveer</groupId>
    <artifactId>disir</artifactId>
    <version>LATEST</version>
</dependency>
```

## License

The MIT License (MIT)

Copyright (c) 2016 Travis Melhiser
 
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
