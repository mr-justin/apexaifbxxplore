path=c:\cygwin\bin;%path%

java -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/dsiutils-1.0.5.jar;./lib/fastutil5-5.1.4.jar;./lib/jakarta-commons-httpclient-3.0.1.jar;./lib/jakarta-commons-lang-2.3.jar;./lib/jakarta-commons-logging-1.1.jar;./lib/law-1.4.jar;./lib/log4j-1.2.14.jar;./lib/openrdf-sesame-2.2-beta2-onejar.jar;./lib/tar.jar; com.ibm.semplore.test.TestLoad config\test.cfg

sh sortun.sh test/hashdata | tee test\hashdata.sort | wc -l
@rem sort -n -S 512m -T . test\hashdata | uniq | tee test\hashdata.sort | wc -l

java -cp ./bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.test.TestIndex config\test.cfg

@rem call buildfacetindex.bat test

@rem java -Xmx1g -cp ./bin;./lib/je-3.3.69.jar com.ibm.semplore.btc.BuildSnippetDB snippet\%1 data.nt

java -cp bin;lib\lucene-core-2.3.2.jar;lib\je-3.3.69.jar;lib\log4j-1.2.14.jar com.ibm.semplore.test.TestEvaluator test\query_rudi.txt > result
diff result test\ans_rudi.txt

java -cp bin;lib\lucene-core-2.3.2.jar;lib\je-3.3.69.jar;lib\log4j-1.2.14.jar com.ibm.semplore.test.TestEvaluator test\query_shanghai.txt > result
diff result test\ans_shanghai.txt

