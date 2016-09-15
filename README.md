# StackWise
StackWise analyzes thread dumps and points out performance issues.  Thread dumps
are a wonderful source of information useful in performance tuning and diagnosing 
syncronization issues. Unfortunately, they are verbose and labor intensive to analyze.
StackWise seeks to save you some analytical time.

If you have trouble or have ideas on how to improve this product.  Please file an [issue](https://github.com/BreakTheMonolith/StackWise/issues).

Things StackWise looks for:
- Threads blocking other threads (synchronization issues)
- Hot Spots (methods in your application most mentioned in running threads)

## System Requirements
- Java 8

All dependancies are included in the executable jar.

## Usage
StackWise can be executed at the command line assuming that java8 is in your path.
```  
java -jar StackWise-core-0.1.1-jar-with-dependencies.jar <options>
```  

I usually put a short command together and put it in my path to make things easier.  For windows:
```  
java -jar C:\Users\TheAshmores\git\StackWise\StackWise-core\target\StackWise-core-0.1.1-jar-with-dependencies.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
```  

### Command Line Options
```  
usage: StackWise
 -f <arg>   input dump file (stdin works as well)
 -h         help
 -o <arg>   output file (stdout is the default)
 -p <arg>   package qualifier (used to abbreviate verbose output)
```  
 
## Sample Output
```  
Package Qualifier=com.jmu  Classes from other packages may be omitted for brevity.

The following threads are blocking other threads from executing.

"http-bio-10.13.0.222-30105-exec-289" - state=RUNNABLE tid=0x000000001bf06000
   Other threads waiting to lock resource <0x0000000680440048>, (a com.sun.beans.WeakCache)
   at java.lang.Class.getDeclaredMethods0(Native Method)
   ...
   at java.beans.Introspector.getPublicDeclaredMethods(Introspector.java:1280)
    - locked <0x0000000680440048> (a com.sun.beans.WeakCache)
   ...
   at java.beans.PropertyDescriptor.getReadMethod(PropertyDescriptor.java:228)
    - locked <0x000000068c71c228> (a java.beans.PropertyDescriptor)
   ...
   at com.jmu.securityclient.ui.filter.AuthenticationFilter.doFilter(AuthenticationFilter.java:105)
   ...
   at com.jmu.scholar.ui.external.filter.VerisignTransactionRecordingFilter.doFilter(VerisignTransactionRecordingFilter.java:120)
   ...
   at com.jmu.common.ui.common.filter.HibernateSessionFilter.doFilter(HibernateSessionFilter.java:53)
   ...
   at com.jmu.common.ui.common.filter.CharsetFilter.doFilter(CharsetFilter.java:69)
   ...
   at org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:310)
    - locked <0x00000007a9f92200> (a org.apache.tomcat.util.net.SocketWrapper)
   ...
"http-bio-10.13.0.222-30105-exec-413" - state=BLOCKED tid=0x000000001b0e0000
   Other threads waiting to lock resource <0x0000000687442ec0>, (a java.beans.PropertyDescriptor)
   at java.beans.Introspector.getPublicDeclaredMethods(Introspector.java:1277)
    - waiting on <0x0000000680440048> (a com.sun.beans.WeakCache)
   ...
   at java.beans.PropertyDescriptor.getReadMethod(PropertyDescriptor.java:228)
    - locked <0x0000000687442ec0> (a java.beans.PropertyDescriptor)
   ...
   at org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:310)
    - locked <0x00000007a9f8f5b0> (a org.apache.tomcat.util.net.SocketWrapper)
   ...
"http-bio-10.13.0.222-30105-exec-428" - state=RUNNABLE tid=0x0000000019d24000
   Other threads waiting to lock resource <0x000000068d62bea8>, (a net.sourceforge.jtds.jdbc.JtdsConnection)
   at net.sourceforge.jtds.jdbc.TdsCore.tds7ResultToken(TdsCore.java:2676)
   ...
   at net.sourceforge.jtds.jdbc.JtdsPreparedStatement.executeQuery(JtdsPreparedStatement.java:1029)
    - locked <0x000000068d62bea8> (a net.sourceforge.jtds.jdbc.JtdsConnection)
   ...
   at com.jmu.common.chronus.entity.Person_$$_jvste0a_2c.getFirstName(Person_$$_jvste0a_2c.java)
   at com.jmu.scholar.ui.external.BreadcrumbBean.getTitle(BreadcrumbBean.java:136)
   ...
   at com.jmu.securityclient.ui.filter.AuthenticationFilter.doFilter(AuthenticationFilter.java:105)
   ...
   at com.jmu.scholar.ui.external.filter.VerisignTransactionRecordingFilter.doFilter(VerisignTransactionRecordingFilter.java:120)
   ...
   at com.jmu.common.ui.common.filter.HibernateSessionFilter.doFilter(HibernateSessionFilter.java:53)
   ...
   at com.jmu.common.ui.common.filter.CharsetFilter.doFilter(CharsetFilter.java:69)
   ...
   at org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:312)
    - locked <0x0000000690981fc0> (a org.apache.tomcat.util.net.SocketWrapper)
   ...

Hot Spot Listing.

21 - com.jmu.common.ui.common.filter.CharsetFilter.doFilter() [CharsetFilter.java]
20 - com.jmu.securityclient.ui.filter.AuthenticationFilter.doFilter() [AuthenticationFilter.java]
20 - com.jmu.scholar.ui.external.filter.VerisignTransactionRecordingFilter.doFilter() [VerisignTransactionRecordingFilter.java]
20 - com.jmu.common.ui.common.filter.HibernateSessionFilter.doFilter() [HibernateSessionFilter.java]
6 - com.jmu.scholar.ui.external.CurrentUserBean.initStudentTermAdmit() [CurrentUserBean.java]
3 - com.jmu.scholar.ui.external.CurrentUserBean.initStudentTermAdmitForStudent() [CurrentUserBean.java]
3 - com.jmu.scholar.ui.external.CurrentUserBean.getInitStudentTermAdmit() [CurrentUserBean.java]
2 - com.jmu.scholar.domain.DisplayFormVORuleFilter.isIncluded() [DisplayFormVORuleFilter.java]
2 - com.jmu.scholar.ui.external.HomePageBean.initFormLists() [HomePageBean.java]
2 - com.jmu.common.util.data.HibernateSqlQueryExecutor.execute() [HibernateSqlQueryExecutor.java]
2 - com.jmu.common.util.data.DatabaseStatementExecutor.executeQuery() [DatabaseStatementExecutor.java]
2 - com.jmu.scholar.ui.external.HomePageBean.getRequiredForms() [HomePageBean.java]
2 - com.jmu.common.domain.AbstractBatchJobBase.execute() [AbstractBatchJobBase.java]
2 - com.jmu.scholar.domain.FormManager.findStudentAssignedForms() [FormManager.java]
2 - com.jmu.common.util.data.HibernateSqlQueryExecutor.executeQuery() [HibernateSqlQueryExecutor.java]
1 - com.jmu.scholar.ui.external.CurrentUserBean.isStudentAccessPermitted() [CurrentUserBean.java]
1 - com.jmu.scholar.dao.StudentAccessDAO.findByAlternateId() [StudentAccessDAO.java]
1 - com.jmu.scholar.util.FreemarkerUtils.createConfiguredFreemarkerTemplate() [FreemarkerUtils.java]
1 - com.jmu.common.util.data.DatabaseStatementExecutor.executeQueryWithIterator() [DatabaseStatementExecutor.java]
1 - com.jmu.scholar.util.FreemarkerUtils.processFreemarkerTemplate() [FreemarkerUtils.java]

Produced by StackWise (https://github.com/BreakTheMonolith/StackWise)

```  

