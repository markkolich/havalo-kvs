# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a relatively lightweight K,V store backed by your local disk for persistent storage.  And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is perfect for testing, maintaining fast indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too heavy.

The latest <a href="https://github.com/markkolich/havalo/downloads">stable version of Havalo is 0.0.3</a>.

## Features

* Zero Configuration &ndash; Drop `havalo.war` into your Servlet container, and get a local K,V store with **nothing else to install**.  For a slightly *more* secure deployment, create one `.properties` file with the right magic in it and place it in your Servlet container's default configuration directory.

* In-Memory Locking &ndash; Completely avoids relying on the filesystem to manage resource locking.  As a result, Havalo manages all locks on `objects` and `repositories` in local memory.  As such, Havalo behaves the same on ext3, ext4, NTFS, NFS Plus, etc.  No matter where you deploy Havalo, you can trust it will do the right thing.

* In-Memory Indexing &ndash; Searchable object indexes are held in memory and flushed to disk as needed.  The size of your object indexes are only limited by the amount of memory available to your Servlet container.

* Trusted Stack &ndash; Written in **Java**, built around **Spring 3.1.3**.  Deployable in any **Servlet 3.0** compatible container.  Tested and verified on Tomcat 7 and Jetty 8.

* Runs in your Existing Servlet Container &ndash; Most "enterprisy" like environments *still* deploy their business logic core in some type of Servlet container.  If you need local K,V storage without installing or configurating any additional software in your stack, chances are good Havalo will just work for you out-of-the box.

* RESTful API &ndash; Once deployed, Havalo immeaditely provides a RESTful API that just makes perfect freakin' sense.

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
  <td>Tomcat 7</td>
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

NOTE: may work with other containers, such as Weblogic or Websphere, but these have **not** been tested.

## Deployment Considerations

Havalo is **not** an off-the-shelf replacement for <a href="http://aws.amazon.com/s3/">Amazon S3</a>, <a href="http://redis.io">Redis</a>, <a href="http://www.project-voldemort.com/voldemort/">Voldemort</a> or <a href="http://cassandra.apache.org/">Apache's Cassandra</a>.  If you need completely fault-tolerant, distributed K,V storage then Havalo is probably not for you.

## API

## Building and Running

Havalo is built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.1</a>.

To clone and build this project, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT 0.12.1 installed and configured on your computer</a>.

The Havalo <a href="https://github.com/markkolich/havalo/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java web-application.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/havalo.git

Run SBT from within your newly cloned *havalo* directory.

    #~> cd havalo
    #~/havalo> sbt
    ...
    havalo:0.0.3>

You will see a `havalo` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `container:start` to start the local Servlet container.  By default the server listens on **port 8080**.

    havalo:0.0.3> container:start
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

    havalo:0.0.3> package
    ...
    [info] Compiling 49 Java sources to ~/havalo/target/classes...
    [info] Packaging ~/havalo/dist/havalo-0.0.3.jar ...
    [info] Done packaging.
    [info] Packaging ~/havalo/dist/havalo-0.0.3.war ...
    [info] Done packaging.
    [success] Total time: 8 s, completed Oct 27, 2012 10:47:38 AM

Note the resulting WAR is placed into the **havalo/dist** directory.  Deploy and enjoy.

To create an Eclipse Java project for Havalo, run `eclipse` in SBT.

    havalo:0.0.3> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] havalo

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well -- all source JAR's are fetched and injected into the Eclipse project automatically.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo/blob/master/LICENSE">LICENSE</a> for details.
