@echo %1: input mapping data(two columns, first column is uri in ds1, second's is in ds2) 
@echo %2: name of ds1
@echo %3: name of ds2
@echo %4: strategy, usually 3
 
java -cp bin Hashify < %1 > mapping.hash
@rem only need sorting when using lucene index
sort -g -k 1 mapping.hash > mapping.sort 

cat mapping.sort | java -cp bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.btc.mapping.MappingIndexPreparer config\datasrc.cfg %2 %3 %4 > mapping.prepared
sort -g -k 1 mapping.prepared > %2_%3
sort -g -k 2 mapping.prepared | gawk "BEGIN {OFS=\"\t\"} {print $2,$1}" > %3_%2
cat %2_%3 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %2_%3_index.head %2_%3_index.map
cat %3_%2 | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %3_%2_index.head %3_%2_index.map

sort -g -k 1 %2.ds > %2.ds.sort
sort -g -k 1 %3.ds > %3.ds.sort
cat %2.ds.sort | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %2_ds.head %2_ds.map
cat %3.ds.sort | java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %3_ds.head %3_ds.map

@rem cleanup
del mapping.hash mapping.sort mapping.prepared %2_%3 %3_%2 %2.ds %3.ds %2.ds.sort %3.ds.sort
