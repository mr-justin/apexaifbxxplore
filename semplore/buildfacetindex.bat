@rem %1 is dataset name
java -Xmx1g -cp bin;./lib/lucene-core-2.3.2.jar;./lib/log4j-1.2.14.jar com.ibm.semplore.test.OutputFacetMapping config\%1.cfg

java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_categories_facet.head %1_categories_facet.map 0 < category.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_relations_facet.head %1_relations_facet.map 0 < relation.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_inverserelations_facet.head %1_inverserelations_facet.map 0 < irelation.mapping

del category.mapping relation.mapping irelation.mapping
