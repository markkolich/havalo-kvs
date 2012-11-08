# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a relatively lightweight K,V store backed by your local disk for persistent storage.  And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is perfect for testing, maintaining fast indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too heavy.

## Features

* Zero Configuration &ndash; Drop `havalo.war` into your Servlet container, get a K,V store with no additional messing around.  For a slightly *more* secure deployment, create one `.properties` file with the right magic in it and drop it into your Servlet container's default configuration directory.

* In-Memory Locking &ndash; Completely avoids relying on the filesystem to manage resource locking.  As a result, Havalo manages all locks on `objects` and `repositories` in local memory.  As such, Havalo behaves the same on ext3, ext4, NTFS, NFS Plus, etc.  No matter where you deploy Havalo, you can trust it will do the right thing.

* Trusted Stack &ndash; Written in **Java**, built around **Spring 3.1.3**.  Deployable in any **Servlet 3.0** compatible container.  Tested on Tomcat and Jetty.

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

## Downsides

## API

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo/blob/master/LICENSE">LICENSE</a> for details.

