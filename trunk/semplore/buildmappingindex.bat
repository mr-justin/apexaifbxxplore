@echo %1: input mapping data(two columns, first column is uri in ds1, second's is in ds2) 
@echo %2: path of index of ds1
@echo %3: path of index of ds2
@echo %4: strategy, usually 3
@echo %5: name of ds1
@echo %6: name of ds2
 
java -cp bin Hashify < %1 > mapping.hash
@rem only need sorting when using lucene index
sort -g -k 1 mapping.hash > mapping.sort 

cat mapping.sort | java -cp bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.btc.mapping.MappingIndexPreparer %2 %3 %4 > mapping.prepared
sort -g -k 1 mapping.prepared > %5_%6
sort -g -k 2 mapping.prepared | gawk "BEGIN {OFS=\"\t\"} {print $2,$1}" > %6_%5
cat %5_%6 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %5_%6_index.head %5_%6_index.map
cat %6_%5 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %6_%5_index.head %6_%5_index.map

@rem cleanup
del mapping.hash mapping.sort mapping.prepared %5_%6 %6_%5