@rem %1: config file
@rem %2: data dir containing hashdata for sort

path = C:\Program Files\Java\jre1.6.0_07\bin;c:\cygwin\bin;%PATH%
date /t
time /t
java -Xmx2g -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/je-3.2.21.jar;./lib/dsiutils-1.0.5.jar;./lib/fastutil5-5.1.4.jar;./lib/jakarta-commons-httpclient-3.0.1.jar;./lib/jakarta-commons-lang-2.3.jar;./lib/jakarta-commons-logging-1.1.jar;./lib/law-1.4.jar;./lib/log4j-1.2.14.jar;./lib/openrdf-sesame-2.2-beta2-onejar.jar;./lib/tar.jar; com.ibm.semplore.test.TestLoad %1
date /t
time /t
dir %2\hashdata
sort -n -S 512m %2\hashdata | uniq | tee %2\hashdata.sort | wc -l
date /t
time /t
java -Xmx3g -Xms3g -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/je-3.2.21.jar com.ibm.semplore.test.TestIndex %1
date /t
time /t
