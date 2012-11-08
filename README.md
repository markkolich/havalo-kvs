# Havalo

A zero configuration, non-distributed key-value store that runs in your existing Servlet container.

Sometimes you just need fast K,V like storage, but don't need full redundancy and scalability (`localhost` will do just fine).  With Havalo, simply drop `havalo.war` into your favorite Servlet container and with almost no configuration you'll have access to a relatively lightweight K,V store backed by your local disk for persistent storage.  And, it has a pleasantly simple RESTful API for your added enjoyment.

Perfect for testing, maintaining indexes of records stored "elsewhere", and almost every other deployment scenario where relational databases are just too much for your applications.

## Benefits

Some things to consider ...

* In-Memory Locking &mdash; Havalo completely avoids relying on the underlying filesystem to manage resource locking.  As a result, Havalo manages all locks on `objects` and `repositories` in local memory.  As such, Havalo behaves the same on ext3, ext4, NTFS, NFS Plus, etc.

## Downsides

## API

