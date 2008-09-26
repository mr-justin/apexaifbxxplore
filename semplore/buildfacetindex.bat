java -Xmx1g -cp bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.test.OutputFacetMapping config\%1.cfg %2

java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_categorys_facet.head %1_categorys_facet.map %2 < category.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_relations_facet.head %1_relations_facet.map %2 < relation.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder %1_inverserelations_facet.head %1_inverserelations_facet.map %2 < irelation.mapping
