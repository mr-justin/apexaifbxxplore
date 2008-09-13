@rem %1: config file
@rem %2: data dir containing hashdata for sort

rem path = c:\progra~1\java\jdk1.5.0_15\bin;%PATH%
path = c:\progra~1\java\jdk1.6.0_06\bin;%PATH%
java -Xmx3g -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/je-3.2.21.jar;./lib/dsiutils-1.0.5.jar;./lib/fastutil5-5.1.4.jar;./lib/jakarta-commons-httpclient-3.0.1.jar;./lib/jakarta-commons-lang-2.3.jar;./lib/jakarta-commons-logging-1.1.jar;./lib/law-1.4.jar;./lib/log4j-1.2.14.jar;./lib/openrdf-sesame-2.2-beta2-onejar.jar;./lib/tar.jar; com.ibm.semplore.test.TestLoad %1

@rem sort using hadoop platform
@rem if you don't have hadoop platform available, you can comment out line49 in com.ibm.semplore.imports.impl.data.preprocess.Main, that will also do the sort
dir %2\hashdata
move %2\hashdata .
scp hashdata root@hadoop5:~/sxr/hashdata
move hashdata %2\hashdata
ssh root@hadoop5 "rm -f ~/sxr/hashdata.sort && hadoop dfs -rmr hashdata hashdata.sort"
ssh root@hadoop5 "hadoop dfs -put ~/sxr/hashdata hashdata"
ssh root@hadoop5 "hadoop jar ~/sxr/sortunique.jar -Dmapred.input.dir=hashdata -Dmapred.output.dir=hashdata.sort -Dmapred.reduce.tasks=1"
ssh root@hadoop5 "hadoop dfs -get hashdata.sort/part-00000 ~/sxr/hashdata.sort"
scp root@hadoop5:~/sxr/hashdata.sort hashdata.sort
move hashdata.sort %2\hashdata.sort


java -Xmx6g -Xms6g -cp ./bin;./lib/lucene-core-2.3.2.jar;./lib/je-3.2.21.jar com.ibm.semplore.test.TestIndex %1

