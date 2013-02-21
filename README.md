# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a fast, relatively lightweight K,V store backed by your local disk for persistent storage.  And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is perfect for testing, maintaining fast indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too heavy.

The latest <a href="http://markkolich.github.com/downloads/havalo/0.4/havalo-0.0.4.war">stable version of Havalo is 0.0.4</a>.

## Features

* **Zero Configuration** &ndash; Drop `havalo.war` into your Servlet container, and get a local K,V store with **nothing else to install**.  For a slightly *more* secure deployment, create one `.properties` file with the right magic in it and place it in your Servlet container's default configuration directory.

* **In-Memory Locking** &ndash; Completely avoids relying on the filesystem to manage resource locking.  As a result, Havalo manages all locks on objects and repositories in local memory.  As such, Havalo behaves the same on ext3, ext4, NTFS, NFS Plus, etc.  No matter where you deploy Havalo, you can trust it will do the right thing.

* **In-Memory Indexing** &ndash; Searchable object indexes are held in memory and flushed to disk as needed.  The size of your object indexes are only limited by the amount of memory available to your Servlet container JVM.

* **Trusted Stack** &ndash; Written in Java, built around Spring 3.1.3.  Deployable in any **Servlet 3.0** compatible container.  Tested and verified on Tomcat 7 and Jetty 8.

* **RESTful API** &ndash; Once deployed, Havalo immediately provides a RESTful API that just makes perfect sense.  All API responses are in pure JSON &mdash; no XML, anywhere. 

* **ETag and If-Match Support** &ndash; All objects are stored with an automatically generated SHA-1 `ETag` hash of the binary object data.  As such, subsequent update operations on that object can be conditional if desired.  In slightly more technical terms, accept a `PUT` for an object only if the SHA-1 hash sent with the `If-Match` HTTP request header matches the existing object `ETag` hash.

* **Havalo Client** &ndash; A Java client for the <a href="https://github.com/markkolich/havalo#api">Havalo API</a> is available off-the-shelf as provided by the <a href="https://github.com/markkolich/havalo-client">havalo-client</a> project.  If you'd rather not use the provided Java client, it's straightforward to write a client for the <a href="https://github.com/markkolich/havalo#api">Havalo API</a> in a language of your choice.

## Compatibility

Havalo is confirmed to work with the following containers:

<table>
  <tr>
  <th>Servlet Engine</th>
  <th>Container</th>
  <th>&nbsp;</th>
  </tr>
  <tr>
  <td rowspan="3">Servlet 3.0</td>
  <td>Tomcat 7*</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
  <tr>
  <td>Jetty 8</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
  <tr>
  <td>Jetty 9</td>
  <td><em>Untested</em></td>
  </tr>
</table>

\* if deploying Havalo inside of Tomcat 7, see note below with regards to URL encoded slashes.

NOTE: may work with other containers, such as Weblogic or Websphere, but these have **not** been tested.

### Using Havalo with Tomcat 7

By default, Tomcat 7 does **not** accept URI's that contain a URL encoded slash (`%2F`) &mdash; incoming request URI's that contain a `%2F` in them are immediately rejected with a `400 Bad Request`.  This behavior appears to be specific to Tomcat.

Therefore, if you intend to use Havalo with Tomcat 7, you must add the following to your `CATALINA_OPTS` environment variable in `bin/startup.sh`:

```bash
-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true
```

## Considerations

Havalo is **not** an off-the-shelf replacement for <a href="http://aws.amazon.com/s3/">Amazon S3</a>, <a href="http://redis.io">Redis</a>, <a href="http://www.project-voldemort.com/voldemort/">Voldemort</a> or <a href="http://cassandra.apache.org/">Apache's Cassandra</a>.  If you need completely fault-tolerant, distributed K,V storage then Havalo is probably not for you.

## Fundamentals

There are a few fundamental constructs to be aware of when using Havalo and its API.

* **Repositories** &ndash; Logical containers that hold objects.  You can think of repositories as a directory on disk that holds a bunch of files.  Each Havalo "user" you create is assigned a unique repository identified under-the-hood by a UUID &mdash; that user, once authenticated, can do whatever they want inside of their repository.

* **Objects** &ndash; A blob of binary data.  Objects are anything you want up to a reasonable maximum of 2GB.  Using the Havalo API, you can attach pieces of arbitrary metadata to an object &mdash; sent to the API in the `Content-Type` and `ETag` HTTP request headers.  If a `Content-Type` is sent with an object to the API, that same `Content-Type` is sent back to the client when the object is retrieved.

## API

Havalo provides a completely RESTful API that lets users `PUT` objects, `GET` objects, and `DELETE` objects in their repositories.  Additionally, administrator level users can also `POST` (create) repositories and `DELETE` repositories.  Note that the user-to-repository relationship is 1:1, meaning creating a repository is equivalent to creating a user, and deleting a repository is equivalent to deleting a user.

### Credentials

When you create a new repository, you're also creating a new user by default.  Again, note that the user-to-repository relationship is 1:1 &mdash; every user maps to a single repository and vice-versa.

On creation, every user is given a key-pair which consists of a unique UUID, and a randomly generated base-64 URL-safe encoded secret.

```java
public final class KeyPair {
  private final UUID key_;
  private final String secret_;
}
```

This key-pair is used to generate the right authentication token for the Havalo API.

### Authentication

Authentication credentials are passed to the Havalo API in the `Authorization` HTTP request header.  The required format of the `Authorization` HTTP request header is as follows:

    Authorization: Havalo RepositoryUUID:Signature

Note the repository UUID is a randomly generated UUID that uniquely represents the user and their repository.

#### Request Signing

The authorization Signature is the result of the following logical function.

    Base64( HMAC-SHA256( UTF-8-Encoding-Of( RepositoryUUID, StringToSign ) ) )

And, StringToSign is the result of the following logical function.

    HTTP-Verb ("GET", "PUT", "POST", or "DELETE") + "\n" +
    RFC-822 Formatted Date (as sent with 'Date' request header, required) + "\n" +
    Content-Type (from 'Content-Type' request header, optional) + "\n" +
    CanonicalizedResource (the part of this request's URL from
      the protocol name up to the query string in the first line
      of the HTTP request)

And lastly, CanonicalizedResource is nothing more than just the "raw path" of the request.

If you're using HttpClient 4.x, the "raw path" is:

```java
final HttpGet request = new HttpGet();
final String canonicalizedResource = request.getURI().getRawPath();
```

Or, if you're thinking about the CanonicalizedResource in context of an `HttpServletRequest` it is:

```java
final String canonicalizedResource = request.getRequestURI();
```

Note your `HMAC-SHA256` signer should be initialized with the key-pair secret.  In Java, this signer is usually an instance of `javax.crypto.Mac`.

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public static final Mac getHmacSHA256Instance(final KeyPair kp) {
  final Mac mac = Mac.getInstance("HmacSHA256");
  mac.init(new SecretKeySpec(kp.getSecret().getBytes("UTF-8")), "HmacSHA256");
  return mac;
}
```

See <a href="https://github.com/markkolich/havalo-client/blob/master/src/main/java/com/kolich/havalo/client/signing/algorithms/HMACSHA256Signer.java">HMACSHA256Signer.java</a> in the <a href="https://github.com/markkolich/havalo-client">havalo-client</a> package for a complete example of wiring together real `HMAC-SHA256` signer.

### Working with Repositories

Only the default administrator of the Havalo application can create and delete repositories.

#### Create a Repository

Create a new repository, and its corresponding owner (user).

    POST:/api/repository

#### Delete a Repository

Delete a repository, and its corresponding owner (user).

    DELETE:/api/repository/{uuid}

#### List Objects in Repository

List all objects in repository, or list only objects in repository that start with a given prefix.

    GET:/api/repository
    GET:/api/repository?startsWith=prefix

### Working with Objects

#### PUT an Object

Upload (`PUT`) an object.

    PUT:/api/object/{key}
    
#### POST an Object (Multipart request)

Multipart `POST` an object.

    POST:/api/object

#### GET an Object

Retrieve (`GET`) an object.

    GET:/api/object/{key}

#### DELETE an Object

Delete an object.

    DELETE:/api/object/{key}

## Building and Running

### Download

You can download the latest version of Havalo on the <a href="http://markkolich.github.com/downloads/havalo">Havalo download page</a>.

### Build from Source

Havalo is built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.2</a>.

To clone and build this project, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT installed and configured on your computer</a>.

The Havalo <a href="https://github.com/markkolich/havalo/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java web-application.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/havalo.git

Run SBT from within your newly cloned *havalo* directory.

    #~> cd havalo
    #~/havalo> sbt
    ...
    havalo:0.0.4>

You will see a `havalo` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `container:start` to start the local Servlet container.  By default the server listens on **port 8080**.

    havalo:0.0.4> container:start
    [info] jetty-8.0.4.v20111024
    [info] started o.e.j.w.WebAppContext{/,[file:~/havalo/src/main/webapp/]}
    [info] Initializing Spring root WebApplicationContext
    ...
    10/27 10:41:33 INFO  [pool-6-thread-2] o.s.w.s.DispatcherServlet -
        FrameworkServlet 'havalo': initialization completed in 60 ms
    [info] Started SelectChannelConnector@0.0.0.0:8080 STARTING
    [success] Total time: 2 s, completed Oct 27, 2012 10:41:33 AM

In your nearest web-browser, visit <a href="http://localhost:8080">http://localhost:8080</a> and you should see the Havalo application homepage &mdash; it's a blank page that says *Havalo*.  The Havalo API endpoint can be found at <a href="http://localhost:8080/api">http://localhost:8080/api</a>.

To stop the development server, run `container:stop`.

See the <a href="https://github.com/siasia/xsbt-web-plugin/wiki">xsbt-web-plugin wiki</a> for all of the gory details on managing the development servlet container from SBT.

In SBT, run `package` to build a deployable WAR for your favorite Servlet container.

    havalo:0.0.4> package
    ...
    [info] Compiling 49 Java sources to ~/havalo/target/classes...
    [info] Packaging ~/havalo/dist/havalo-0.0.4.jar ...
    [info] Done packaging.
    [info] Packaging ~/havalo/dist/havalo-0.0.4.war ...
    [info] Done packaging.
    [success] Total time: 8 s, completed Oct 27, 2012 10:47:38 AM

Note the resulting WAR is placed into the **havalo/dist** directory.  Deploy and enjoy.

To create an Eclipse Java project for Havalo, run `eclipse` in SBT.

    havalo:0.0.4> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] havalo

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well &mdash; all source JAR's are fetched and injected into the Eclipse project automatically.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo/blob/master/LICENSE">LICENSE</a> for details.
