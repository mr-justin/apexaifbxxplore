package org.aifb.xxplore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.aifb.xxplore.shared.util.URIHelper;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.xmedia.accessknow.sesame.persistence.ConnectionProvider;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.IDataSource;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.QueryEvaluatorUnavailableException;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

/**
 * Class for launching the XXPlore-Enviroment from the commandline. Clearly, not all features are available.
 * Currently supported is: 
 * 		* keyword search
 * 		* ...
 *
 */

public class XXPloreCommandline {
	
	public static void main(String[] args){
		
		CommandlineHelper consoleHelper = (new XXPloreCommandline()).new CommandlineHelper();
		
//		retrieve parameters ...
		consoleHelper.retrieveParameters();
//		load data
		consoleHelper.prepareRepository();
//		prepare factresult search
		consoleHelper.prepareFactResultSearch();		
//		start XXPlore main-view
		consoleHelper.switchToMainView();
		
	}
	
	
	private class CommandlineHelper{
		
		private static final int SELECTION_KEYWORD_SEARCH = 1;
		private static final int SELECTION_EXIT = 3;
		
		private static final String BACK_TO_MAIN = "-M";
		
		private Properties m_parameters;
		private ModelDefinitionContentProvider m_modeldef_provider;
		private IDataSource m_onto;
		private ModelDefinition m_modeldefinition;
		
		
		private void retrieveParameters(){
			
			State.CURRENT_STATE = State.XXPLORE_START;

			print("Please enter the path to your configurationfile (*.ods). use '/' instead '\\'. " +
					"If your path includes spaces, quotes would be great. E.g. \"c:/my files/myConf.ods.\".",false);

			String input = getUserInput();

			File file = new File(input);
			m_parameters = new Properties();
			
			if(file.exists()){
				
				try {
					
					BufferedReader bis = new BufferedReader(new FileReader(file));
					String next_line;					
					
					while((next_line = bis.readLine())!= null){						
						String key = next_line.substring(0, next_line.indexOf("="));
						String value = next_line.substring(next_line.indexOf("=")+1);
						m_parameters.put(key, value);
					}

					State.CURRENT_STATE = State.PARAMETERS_LOADED;
				} 
				catch(IOException e){
					e.printStackTrace();
				} 
			}
			else{
				print("Can not find configuration file using path:'"+input+". " +
						"Please restart application and provide a valid path.",false);
				
				System.exit(1);
			}
		}
		
		private void prepareRepository(){
			
			Emergency.checkPrecondition(State.CURRENT_STATE == State.PARAMETERS_LOADED, "Parameters not loaded yet. " +
					"\nPlease do so before preparing repository.");
			
			//create conncetion provider
			IConnectionProvider provider = null;
			String providerClass;
			
			if(m_parameters.get(KbEnvironment.CONNECTION_PROVIDER_CLASS) != null){
				providerClass = m_parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
			}
			else{
				providerClass = KbEnvironment.DEFAULT_CONNECTION_PROVIDER_CLASS;
			}

			try {
				provider = (IConnectionProvider)Class.forName(providerClass).newInstance();
			} 
			catch (Exception e) {
				IStatus status = new MultiStatus(ExplorePlugin.PLUGIN_ID,
						ExplorePlugin.LOADING_DATASOURCE_ERROR,"The definition could not be correctly loaded",e);
				try {
					throw new CoreException(status);
				} 
				catch (CoreException e1) {
					e1.printStackTrace();
				}
			}

			provider.configure(m_parameters);		
			
			IOntology onto = null;
			try {
				if (provider instanceof ConnectionProvider){
					onto = provider.getConnection().loadOrCreateOntology(PropertyUtils.convertToMap(m_parameters));
				}
				else if (provider instanceof Kaon2ConnectionProvider){
					onto = provider.getConnection().loadOntology(PropertyUtils.convertToMap(m_parameters));
				}

			} catch (DatasourceException e) {
				e.printStackTrace();
			} catch (MissingParameterException e) {
				e.printStackTrace();
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			} catch (OntologyCreationException e) {
				e.printStackTrace();
			}catch (OntologyLoadException e) {
				e.printStackTrace();
			}

			if (provider instanceof ConnectionProvider) {

				try {
					addFileToRepository(onto,PropertyUtils.convertToMap(m_parameters));
				} catch (MissingParameterException e1) {
					e1.printStackTrace();
				}
				SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
				ISession session = null;

				try {
					session = sesame_factory.openSession(provider.getConnection(), onto);
				} catch (DatasourceException e) {
					e.printStackTrace();
				} catch (OpenSessionException e) {
					e.printStackTrace();
				}
				
				//set dao manager
				PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
				session.close();			
			}
			else if (provider instanceof Kaon2ConnectionProvider) {
				
				ISessionFactory factory = SessionFactory.getInstance();
				factory.configure(PropertyUtils.convertToMap(m_parameters));
				PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());
				
			}

			ISessionFactory factory = SessionFactory.getInstance();
			PersistenceUtil.setSessionFactory(factory); 
			//open a new session with the ontology
			try {
				factory.openSession(provider.getConnection(),onto);
			} catch (DatasourceException e) {
				e.printStackTrace();
			} catch (OpenSessionException e) {
				e.printStackTrace();
			}
			
			State.CURRENT_STATE = State.REPOSITORY_READY;
			
			m_onto = onto;
		}
		
		private void addFileToRepository(IOntology onto, Map<String, Object> parameters)throws MissingParameterException{
			
			if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
				throw new MissingParameterException("Ontology file path is missing.");
			}
			
			if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_NAME)) {
				throw new MissingParameterException("Ontology filename is missing.");
			}
			
			if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
				throw new MissingParameterException("Ontology base-uri is missing.");
			}
			
			if(!parameters.containsKey(ExploreEnvironment.LANGUAGE)) {
				throw new MissingParameterException("Ontology language is missing.");
			}
			
//			Check whether file has already been added to repository 
			
			File repositoryDir = new File(SesameRepositoryFactory.REPO_PATH_DEFAULT);
			String path = repositoryDir.getAbsolutePath()+"/"+parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added";
			boolean already_added = Arrays.asList(repositoryDir.list()).contains(parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added");
			
			if(already_added) {
				return;
			} else{
				try {
					(new File(path)).createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
			String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
			String language = (String)parameters.get(ExploreEnvironment.LANGUAGE);
						
//			File has not been added -> add it now
			
			try {
				onto.importOntology(language, baseUri, new FileReader(filePath));
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
			catch (OntologyImportException e) {
				e.printStackTrace();
			}
		}
		
		private void switchToMainView(){
			
			State.CURRENT_STATE = State.MAIN_MENU;	
			
			print("xxplore main menu", true);
			print("Select one of the options below, by providing the appropriate number. " +
					"E.g. for switching to keyword search type '1'.",false);
			print("[1]: keyword search.", false);
			print("[2]: ... (more to come).", false);
			print("[3]: exit.", false);

			String input = getUserInput();
			Boolean is_valid = validateInput(input);
			
			while(!is_valid){
				
				print("please provide a valid input. Enter selection below ... ",false);
				input = getUserInput();
				is_valid = validateInput(input);
				
			}

			switch(Integer.parseInt(input)){
			
				case SELECTION_KEYWORD_SEARCH: switchToKeywordSearchView();break;
				case SELECTION_EXIT: System.exit(1); break;
				default: print("error. selection not defined.",false);
				
			}
		}
		
		private void switchToKeywordSearchView(){
			
			State.CURRENT_STATE = State.KEYWORD_SEARCH;
			
			print("xxplore keyword search", true);		
			start_keywordSearch();
			
		}
		
		private void start_keywordSearch(){
			
			print("Please type your query in below. To go back to main menu type '-m'.",false);
			
			String input = getUserInput();
			Boolean is_valid = validateInput(input);
			
			while(!is_valid){
				
				print("please provide a valid input. Enter query below ... ",false);
				input = getUserInput();
				is_valid = validateInput(input);
				
			}
			
			if(input.equals(BACK_TO_MAIN)||input.equals(BACK_TO_MAIN.toLowerCase())){
				switchToMainView();
			}
			else{
				
				if(m_modeldef_provider.getModelDefinition() != null){
					m_modeldef_provider.getModelDefinition().clear();
				}
				
				m_modeldef_provider.updateDefinition(input);
				boolean has_results = m_modeldef_provider.updateIntepretationCommandlineVersion();
				
				if(has_results){
					
					m_modeldef_provider.updateResult();					
					IQueryEvaluator eval;
					
					try {
						
						eval = PersistenceUtil.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
						IQueryResult queryResult = eval.evaluate(m_modeldef_provider.getModelDefinition().getDLQuery());
						print(queryResult,input);
						
						start_keywordSearch();
					} 
					catch (QueryEvaluatorUnavailableException e) {
						e.printStackTrace();
						System.exit(1);
					}
					catch (QueryException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				else{
					start_keywordSearch();
				}
			}			
		}
		
		private String getUserInput(){		
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = new String();

			try {
				System.out.print("[XXPLORE > INPUT]");
				input = br.readLine();
				System.out.println();
			} 
			catch (IOException e){				
			}
			
			return input;
		}
		
		private int[] m_validInputMainView = {1,3};
		
		private boolean validateInput(String input){					
			
			if(State.CURRENT_STATE == State.MAIN_MENU){				
				if(Arrays.binarySearch(m_validInputMainView,Integer.parseInt(input)) >= 0){
					return true;
				}
				else{
					return false;
				}
			}
			else if(State.CURRENT_STATE == State.KEYWORD_SEARCH){
				if(!input.equals("")){
					return true;
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}			
		}
		
		private void prepareFactResultSearch(){
			
			m_modeldef_provider = ModelDefinitionContentProvider.ModelDefinitionContentProviderSingleTonHolder.getInstance();
			m_modeldefinition = new ModelDefinition(m_onto);
			m_modeldef_provider.setModelDefinition(m_modeldefinition);
			m_modeldef_provider.makeKbIndexCommandLineVersion(((IOntology)m_modeldefinition.getDataSource()).getUri());
			
		}
		
		private void print(IQueryResult queryResult, String query){
			
			String prefix = "[XXPLORE > RESULTS";
			
			Set<ITuple> result_tuples = queryResult.getResult();
			Iterator<ITuple> iter = result_tuples.iterator();
			
//			print column labels
			
			String labels = new String();
			
			for(int i = 0; i < queryResult.getQueryVariables().length; i++) {
				
				String this_var = queryResult.getQueryVariables()[i];				
				labels += this_var;
				
				if(i != queryResult.getQueryVariables().length-1){
					labels += " || ";
				}
			}
			
			System.out.println();
			System.out.println();
			
			printDivider("=");
			System.out.println(prefix+"] RESULTS FOR QUERY: '"+query+"'");
			printDivider();
			System.out.println(prefix+" > VARIABLES]"+labels);			
			printDivider();
			
//			print results
			while(iter.hasNext()){
				
				ITuple tuple = iter.next();
				String this_row = new String();
					
				for(int i = 0; i < tuple.getArity(); i++){
					
					this_row += URIHelper.truncateUri(tuple.getElementAt(i).toString());
					
					if(i != tuple.getArity()-1){
						this_row += " || ";
					}
				}			
				
				System.out.println(prefix+" > TUPLE]"+this_row);
			}
			
			printDivider();
		}
		
		private void printDivider(){			
			printDivider("-");
		}
		
		private void printDivider(String divider_symbol){
			
			int divider_length = 70;

			for(int i = 0; i < divider_length; i++){				
				System.out.print(divider_symbol);
			}
			
			System.out.println();	
		}
	
		private void print(String to_print, boolean is_Title){

			StringBuffer strgBuffer = new StringBuffer(to_print);			
//			int line_length = 150;
			String prefix = "[XXPLORE";

			switch(State.CURRENT_STATE){
			
				case State.MAIN_MENU: prefix+=" > MAIN MENU]"; break;
				case State.KEYWORD_SEARCH: prefix+=" > KEYWORD SEARCH]"; break;
				default: prefix+="]"; break;
				
			}
			
			if(is_Title){
				
				System.out.println();
				System.out.println();
				System.out.println();
				printDivider("#");
				System.out.println(prefix+strgBuffer.toString().toUpperCase());
				printDivider("#");
				System.out.println();				
			}
			else{
				
				strgBuffer.insert(0, prefix);				
//				if(strgBuffer.length() > line_length){				
//					for(int i = 0; i < strgBuffer.length(); i += line_length){					
//						strgBuffer.insert(i, "\n");					
//					}
//				}
				System.out.println(strgBuffer.toString().toUpperCase());
			}
		}
	}
	
	private static class State{		
		private static final int XXPLORE_START = 0;
		private static final int PARAMETERS_LOADED = 1;
		private static final int REPOSITORY_READY = 2;
		private static final int MAIN_MENU = 3;
		private static final int KEYWORD_SEARCH = 4;
	
		private static int CURRENT_STATE;
	}
}