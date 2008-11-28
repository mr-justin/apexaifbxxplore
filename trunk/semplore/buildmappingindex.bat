@echo %1: input mapping data(two columns, first column is uri in ds1, second's is in ds2) 
@echo %2: name of ds1
@echo %3: name of ds2
@echo %4: strategy, usually 3
 
java -cp bin Hashify < %1 | sort -n -k 1 | uniq | java -Xmx1g -cp bin;./lib/lucene-core-2.3.2.jar;./lib/log4j-1.2.14.jar com.ibm.semplore.btc.mapping.MappingIndexPreparer datasrc.cfg %2 %3 %4 > mapping.prepared
sort -n -k 1 mapping.prepared > %2_%3
sort -n -k 2 mapping.prepared | gawk "BEGIN {OFS=\"\t\"} {print $2,$1}" > %3_%2
cat %2_%3 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %2_%3_index.head %2_%3_index.map 0
cat %3_%2 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %3_%2_index.head %3_%2_index.map 0

@rem cleanup
pause
@rem del mapping.hash mapping.sort 
del mapping.prepared %2_%3 %3_%2
