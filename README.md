# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a relatively lightweight K,V store backed by your local disk for persistent storage.  And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is perfect for testing, maintaining fast indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too heavy.

## Features

* Zero Configuration &ndash; Drop `havalo.war` into your Servlet container, get a K,V store with no additional messing around.  For a slightly *more* secure deployment, create one `.properties` file with the right magic in it and drop it into your Servlet container's default configuration directory.

* In-Memory Locking &ndash; Completely avoids relying on the filesystem to manage resource locking.  As a result, Havalo manages all locks on `objects` and `repositories` in local memory.  As such, Havalo behaves the same on ext3, ext4, NTFS, NFS Plus, etc.  No matter where you deploy Havalo, you can trust it will do the right thing.

* Trusted Stack &ndash; Written in **Java**, built around **Spring 3.1.3**.  Deployable in any **Servlet 3.0** compatible container.  Tested on Tomcat and Jetty.

## Downsides

## API

