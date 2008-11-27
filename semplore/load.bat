@rem %1: dataset
@rem %2: data dir as in config\dataset.cfg

path = C:\Program Files\Java\jre1.6.0_07\bin;c:\cygwin\bin;%PATH%

java -Xmx2g -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/dsiutils-1.0.5.jar;./lib/fastutil5-5.1.4.jar;./lib/jakarta-commons-httpclient-3.0.1.jar;./lib/jakarta-commons-lang-2.3.jar;./lib/jakarta-commons-logging-1.1.jar;./lib/law-1.4.jar;./lib/log4j-1.2.14.jar;./lib/openrdf-sesame-2.2-beta2-onejar.jar;./lib/tar.jar; com.ibm.semplore.test.TestLoad config\%1.cfg

dir %2/hashdata
sh sortun.sh %2/hashdata | tee %2/hashdata.sort | wc -l
@rem sort -n -S 512m -T . --compress-prog=gzip %2\hashdata | uniq | tee %2\hashdata.sort | wc -l

java -Xmx3g -cp ./bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.test.TestIndex config\%1.cfg

@rem call buildfacetindex.bat %1

@rem java -Xmx1g -cp ./bin;./lib/je-3.3.69.jar com.ibm.semplore.btc.BuildSnippetDB snippet\%1 data.nt
