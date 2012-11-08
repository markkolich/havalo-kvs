# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a relatively lightweight K,V store backed by your local disk for persistent storage.  And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is perfect for testing, maintaining fast indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too heavy.

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

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo/blob/master/LICENSE">LICENSE</a> for details.

