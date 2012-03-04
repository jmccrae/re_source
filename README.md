Re_source framework for publishing linked data
==============================================

Re_source is a light framework for publishing legacy resources as linked data. This project is intended to be a template for publishing resources, so instead of providing a download you should fork this project and modify it to meet your needs.

Publishing a simple linked data resource
----------------------------------------

The main project is called "core" once this project has been checked out it can be opened with NetBeans, Eclipse (choose "Import existing Maven Project") or on the command line as follows (you will need to have [Maven][http://maven.apache.org] installed).

    mvn install

Assuming your resource is an XML folder simply copy it to [core/src/main/webapp/WEB-INF/data], the mapping will be automatic as follows:

    WEB-INF/data/test.xml  -> http://myserver.com/context_root/resource/test.xml

