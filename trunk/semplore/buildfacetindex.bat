java -Xmx1g -cp bin;./lib/lucene-core-2.3.2.jar com.ibm.semplore.test.OutputFacetMapping config\%1.cfg %2

java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder wordnet_category_facet.head %1_category_facet.map %2 < category.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder wordnet_category_facet.head %1_relation_facet.map %2 < relation.mapping
java -cp bin com.ibm.semplore.btc.mapping.MappingIndexBuilder wordnet_category_facet.head %1_irealtion_facet.map %2 < irelation.mapping
