# Havalo-KVS

A zero configuration, non-distributed NoSQL key-value store that runs in any Servlet 3.0 compatible container.

Sometimes you just need fast NoSQL storage, but don't need full redundancy and scalability (that's right, `localhost` will do just fine).  With Havalo-KVS, simply drop `havalo-kvs.war` into your favorite Servlet 3.0 compatible container and with <a href="#deployment">almost no configuration</a> you'll have access to a fast and lightweight K,V store backed by any local mount point for persistent storage.  And, Havalo-KVS has a pleasantly simple RESTful API for your added enjoyment.

Havalo-KVS is perfect for testing, maintaining fast indexes of data stored "elsewhere", and almost any other deployment scenario where relational databases are just too heavy.

See the <a href="releases">Releases</a> page for the latest version.

Written in Java 8, for a Java 8 compatible JVM.

## Features

* **Zero Configuration** &ndash; Drop `havalo-kvs.war` into your Servlet 3.0 compatible container, and get local NoSQL storage with nothing else to install &mdash; Havalo-KVS is built to run alongside your exsting applications.  For a slightly more secure deployment, create one `.conf` file with the right magic inside and place it in your Servlet container's default configuration directory.

* **In-Memory Locking** &ndash; Completely avoids relying on the filesystem to manage resource locking.  As a result, Havalo-KVS manages all locks on objects and repositories in local memory.  As such, Havalo-KVS behaves the same on ext3, ext4, NTFS, NFS Plus, etc.  No matter where you deploy Havalo-KVS, you can trust it will do the right thing.

* **In-Memory Indexing** &ndash; Searchable object indexes are held in memory and flushed to disk as needed.  The size of your object indexes are only limited by the amount of memory available to your Servlet container JVM.

* **Trusted Stack** &ndash; Written in Java, built around *raw asynchronous* Servlet's with no "bloated frameworks" to get in the way.  Deployable in any **Servlet 3.0** compatible container.  Fully tested and qualified on Tomcat 7, Tomcat 8, Jetty 8, and Jetty 9.

* **RESTful API** &ndash; Havalo-KVS offers a <a href="#api">simple RESTful API that just makes perfect sense</a>.  All API responses are in pure JSON &mdash; no XML, anywhere.

* **ETag and If-Match Support** &ndash; All objects are stored with an automatically generated SHA-1 `ETag` hash of the binary object data.  As such, subsequent update operations on that object can be conditional if desired.  In slightly more technical terms, accept a `PUT` for an object only if the SHA-1 hash sent with the `If-Match` HTTP request header matches the existing object `ETag` hash.

* **Havalo-KVS Client** &ndash; A Java client for the <a href="#api">Havalo-KVS API</a> is available off-the-shelf as provided by the <a href="https://github.com/markkolich/havalo-kvs-client">havalo-kvs-client</a> project.  If you'd rather not use the provided Java client, it's straightforward to write a client for the <a href="#api">Havalo-KVS API</a> in a language of your choice.

## Compatibility

Havalo-KVS is confirmed to work with the following containers:

<table>
  <tr>
  <th>Servlet Engine</th>
  <th>Container</th>
  <th>&nbsp;</th>
  </tr>
  <tr>
  <td rowspan="4">Servlet 3.0</td>
  <td>Tomcat 8*</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
  <tr>
  <td>Tomcat 7*</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
  <tr>
  <td>Jetty 8</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
  <tr>
  <td>Jetty 9</td>
  <td><img src="http://openclipart.org/image/800px/svg_to_png/161503/OK-1.png" height="20"></td>
  </tr>
</table>

\* if deploying Havalo-KVS inside of Tomcat 7/8, see note below with regards to URL encoded slashes.

NOTE: may work with other containers, such as Weblogic or Websphere, but these have **not** been tested.

### Using Havalo-KVS with Tomcat 7/8

By default, Tomcat 7 and 8 do **not** accept URI's that contain a URL encoded slash (`%2F`) &mdash; incoming request URI's that contain a `%2F` in them are immediately rejected with a `400 Bad Request`.  This behavior appears to be specific to Tomcat.

Therefore, if you intend to use Havalo-KVS with Tomcat 7 or 8, you must add the following to your `CATALINA_OPTS` environment variable in `bin/startup.sh`:

```bash
-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true
```

## Considerations

Havalo-KVS is **not** an off-the-shelf replacement for <a href="http://aws.amazon.com/s3/">Amazon S3</a>, <a href="http://redis.io">Redis</a>, <a href="http://www.project-voldemort.com/voldemort/">Voldemort</a> or <a href="http://cassandra.apache.org/">Apache's Cassandra</a>.  If you need completely fault-tolerant, distributed K,V storage then Havalo-KVS is probably not for you.

## Deployment

Deploying Havalo-KVS into your environment is a snap.

You have two deployment options:

1. Default &ndash; Drop the Havalo-KVS `.war` file into your Servlet container's `webapps` directory, and away you go.  No JVM restart is needed unless required by your container.
2. Custom &ndash; Create a custom `havalo-kvs.conf` file and place it into your Servlet container's `conf` directory.  Then, drop the Havalo-KVS `.war` file into your Servlet container's `webapps` directory.  Note, if you are using the Tomcat Manager to deploy and manage your applications in Tomcat, you can also deploy the Havalo-KVS `.war` using the Tomcat Manager interface.

### Default

If desired, Havalo-KVS supports "hot deployment" which allows you to deploy or undeploy the application without stopping your Servlet container.

1. Download the <a href="http://markkolich.github.com/downloads/havalo-kvs">latest version</a> of Havalo-KVS.  This is a `.war` file you will drop into your Servlet container.
2. Copy the `.war` file into your Servlet container's `webapps` directory.
3. Start your Servlet container, if not already running.

### Custom

Havalo-KVS is configured using the HOCON configuration format provided by the <a href="https://github.com/typesafehub/config">Typesafe Config</a> library.  Read more about HOCON and its similarities to JSON <a href="https://github.com/typesafehub/config#json-superset">here</a>.

The Havalo-KVS default configuration file, <a href="https://github.com/markkolich/havalo-kvs/blob/master/src/main/resources/application.conf">application.conf</a>, is shipped inside of the Havalo-KVS `.war` file.  To override any of these configuration properties, simply drop a file named `havalo-kvs.conf` into your Servlet container's `conf` directory.  For example, if running Havalo-KVS inside of Tomcat, drop your custom `havalo-kvs.conf` into `$CATALINA_HOME/conf` before deploying `havalo-kvs.war`.

Finally, note you only need to override the configuration properties you want to change.  For example, if you only want to override the location on disk where Havalo-KVS stores its repositories and objects, create a `havalo-kvs.conf` file that looks like this:

```no-highlight
havalo {
  api {
    admin.uuid = "your admin UUID goes here"
    admin.secret = "your admin API secret goes here"
  }
  repository {
    base = "/path/to/your/havalo/root"
  }
}
```

For a complete list of configurable properties and their description, see the <a href="https://github.com/markkolich/havalo-kvs/blob/master/src/main/resources/application.conf">Havalo-KVS default application.conf</a>.

### Security

If you plan to use Havalo-KVS in a real production environment exposed to the world, you should be sure to change the `havalo.api.admin.uuid` and `havalo.api.admin.secret` configuration properties.

The `havalo.api.admin.uuid` and `havalo.api.admin.secret` configuration properties define the default administrator user credentials that are used to create/delete users and repositories.  These should be set to something "unguessable" and unique to your environment before Havalo-KVS deployment.

## Fundamentals

There are a few fundamental constructs to be aware of when using Havalo-KVS and its API.

* **Repositories** &ndash; Logical containers that hold objects.  You can think of repositories as a directory on disk that holds a bunch of files.  Each Havalo-KVS "user" you create is assigned a unique repository identified under-the-hood by a UUID &mdash; that user, once authenticated, can do whatever they want inside of their repository.  Note that the user-to-repository relationship is 1:1, meaning creating a repository is equivalent to creating a user, and deleting a repository is equivalent to deleting a user.

* **Objects** &ndash; A blob of binary data.  Objects are anything you want up to a reasonable maximum of 2GB.  Using the Havalo-KVS API, you can attach pieces of arbitrary metadata to an object &mdash; sent to the API in the `Content-Type` and `ETag` HTTP request headers.  If a `Content-Type` is sent with an object to the API, that same `Content-Type` is sent back to the client when the object is retrieved.

## API

Havalo-KVS provides a completely RESTful API that lets users `PUT` objects, `GET` objects, and `DELETE` objects in their repositories.  Additionally, administrator level users can also `POST` (create) repositories and `DELETE` repositories.

### Credentials

When you create a new repository, you're also creating a new user by default.  Again, note that the user-to-repository relationship is 1:1 &mdash; every user maps to a single repository and vice-versa.

On creation, every user is given a key-pair which consists of a unique UUID, and a randomly generated base-64 URL-safe encoded secret.

```java
public final class KeyPair {
  private final UUID key_;
  private final String secret_;
}
```

This key-pair is used to generate the right authentication token for the Havalo-KVS API.

### Authentication

Authentication credentials are passed to the Havalo-KVS API in the `Authorization` HTTP request header.  The required format of the `Authorization` HTTP request header is as follows:

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

See <a href="https://github.com/markkolich/havalo-kvs-client/blob/master/src/main/java/com/kolich/havalo/client/signing/algorithms/HMACSHA256Signer.java">HMACSHA256Signer.java</a> in the <a href="https://github.com/markkolich/havalo-kvs-client">havalo-kvs-client</a> package for a complete example of wiring together real `HMAC-SHA256` signer.

### Working with Repositories

Only the default administrator of the Havalo-KVS application can create and delete repositories.

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
    
#### GET an Object

Retrieve (`GET`) an object.

    GET:/api/object/{key}

#### DELETE an Object

Delete an object.

    DELETE:/api/object/{key}

## Building and Running

### Download

You can download the latest version of Havalo-KVS on the <a href="http://markkolich.github.com/downloads/havalo-kvs">Havalo-KVS download page</a>.

### Build from Source

Havalo-KVS is built and managed using Maven.

To clone and build this project, you must have Maven installed and configured.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/havalo-kvs.git

Run `mvn package` from within your newly cloned *havalo-kvs* directory to compile and build a deployable WAR.

    #~> cd havalo-kvs
    #~/havalo-kvs> mvn package 

To start the local Servlet container, run `mvn jetty:run`.  By default the server listens on **port 8080**.

    #~/havalo-kvs> mvn jetty:run

In your nearest web-browser, visit <a href="http://localhost:8080/havalo-kvs">http://localhost:8080/havalo-kvs</a> and you should see the Havalo-KVS application homepage &mdash; it's a plain page that says *Havalo-KVS*.  When running locally, the Havalo-KVS API endpoint can be found at <a href="http://localhost:8080/havalo-kvs/api">http://localhost:8080/havalo-kvs/api</a>.

## Licensing

Copyright (c) 2015 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo-kvs/blob/master/LICENSE">LICENSE</a> for details.
