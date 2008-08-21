package edu.unika.aifb.foam.complex;

import java.util.Vector;

import edu.unika.aifb.foam.agenda.Agenda;
import edu.unika.aifb.foam.agenda.AgendaElement;
import edu.unika.aifb.foam.complex.BidirectionalCompleteAgenda;
import edu.unika.aifb.foam.combination.Combination;
import edu.unika.aifb.foam.complex.ComplexCombination;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.result.ResultListImpl;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.complex.ComplexRules;
import edu.unika.aifb.foam.util.UserInterface;

/**
 */
public class ComplexAlign{

	public ComplexAlign() {
		UserInterface.print("COMPLEX ONTOLOGY ALIGNMENT (Pengyun)\n\n");
	}
	
	public Structure ontology;										//ontologies to compare		
	public ResultList resultListLatestSimilar;								//similarities of entities
	public ResultList resultListLatestSub = new ResultListImpl(5);
	public Vector alignments;										//vector of results
	public String name = "";						
	
	public void align() {									
		UserInterface.print("\nAlignment process ("+name+") started");
		ResultTable lastresultSimilar = new ResultTableImpl();				//transcription of similarity results
		lastresultSimilar.copy(resultListLatestSimilar,5,0.05);		
		
		ComplexRules rulesSubsumption = new ComplexRules();
		//encoding of individual rules for one relation (here subsumption)
		Combination combinationSubsumption = new ComplexCombination(rulesSubsumption.total());	//combination of rules
		((ComplexRules)rulesSubsumption).setPreviousResultSimilar(lastresultSimilar);				//similarity is handed to rules
		ResultList resultListSubsumption = new ResultListImpl(5);	//empty list for results		
		
		for (int i = 0; i<2; i++) {
		System.out.println();
		
		ResultTable lastresultSub = new ResultTableImpl();
		lastresultSub.copy(resultListLatestSub,5,0.05);
		rulesSubsumption.setPreviousResultSubsumption(lastresultSub);
		resultListSubsumption = new ResultListImpl(5);	//empty list for results
		
		//bidirectional 
		//Agenda agenda = new CompleteAgenda();	
		Agenda agenda = new BidirectionalCompleteAgenda();
		
		//all entity pairs are compared
		agenda.create(ontology,Parameter.EXTERNAL);		

		int counter = 0;			
		agenda.iterate();			
		while (agenda.hasNext()) {									//comparison starts
			AgendaElement element = agenda.next();		
			Object object1 = element.object1;						//object1
			Object object2 = element.object2;						//object2
//			Object action = element.action;							//what typ of comparison
			counter++;
			if (counter%1000==0) {
				UserInterface.print("|");
				if (counter%100000==0) {UserInterface.print("\n");}
			}		
			combinationSubsumption.reset();
			combinationSubsumption.setObjects(object1,object2);	
			

			for (int j=0; j<rulesSubsumption.total(); j++) {		//go through individual rules
				combinationSubsumption.setValue(j,rulesSubsumption.process(object1,object2,j,ontology));	
			}
			combinationSubsumption.process();						//combine the rules according to strategy
			if (combinationSubsumption.result()>0.01) {				//save result if above threshold
				resultListSubsumption.set(object1,object2,combinationSubsumption.result(),combinationSubsumption.getAddInfo());	
			}
		}
		resultListLatestSub = resultListSubsumption;
		}
		
		alignments = resultListSubsumption.vectorResult();			//transcribe results for saving	
	}	
	
}
