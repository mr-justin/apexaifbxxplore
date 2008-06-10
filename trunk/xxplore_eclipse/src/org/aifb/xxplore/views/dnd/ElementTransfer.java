/**
 * 
 */
package org.aifb.xxplore.views.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.model.impl.PropertyMember;

/**
 * This class is used to implement the Drag And Drop functionalities for the Explore framework
 * @author Julien Tane
 *
 */

public class ElementTransfer extends  ByteArrayTransfer{


	private static ElementTransfer _elementTransfer;

	private static final String CONCEPT = NamedConcept.class.toString();	
	private static final String INSTANCE = NamedIndividual.class.toString();
	private static final String PROPERTY = Property.class.toString();
	private static final String PROPERTY_INSTANCE = PropertyMember.class.toString();

	private static final String MODEL="Model";
	private static final String QUERY="Query";
	private static final String ISELECTION="ISelection";

	private static final int CLASSID = registerType(CONCEPT);
	private static final int INSTANCEID=registerType(INSTANCE);
	private static final int PROPERTYID=registerType(PROPERTY);
	private static final int PROPERTYINSTANCEID=registerType(PROPERTY_INSTANCE);
	private static final int MODELID=registerType(MODEL);
	private static final int QUERYID=registerType(QUERY);
	private static final int ISELECTIONID=registerType(ISELECTION);


//	protected int currentsourceid = -1;
//	protected IOntology source = null;

	public static ElementTransfer getInstance() {
		if (_elementTransfer== null)
			_elementTransfer= new ElementTransfer();
		return _elementTransfer;
	}


	@Override
	protected int[] getTypeIds() {
		int[] ids= new int[]{ ISELECTIONID ,CLASSID, INSTANCEID,PROPERTYID, PROPERTYINSTANCEID, MODELID,QUERYID};
		return ids;
	}

	@Override
	protected String[] getTypeNames() {
		return new String[]{ISELECTION,CONCEPT, INSTANCE, PROPERTY, PROPERTY_INSTANCE, MODEL, QUERY};
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (object != null && object instanceof IResource[]) return; 

		if (isSupportedType(transferData)){
			Object[] resources = new Object[0]; 
			if(object instanceof LinkedList){
				for(int i = 0; i < ((LinkedList)object).size(); i++){
					Object resource = ((LinkedList)object).get(i); 
					Object[] newRess = new Object[resources.length + 1];
					System.arraycopy(resources, 0, newRess, 0, resources.length);
					newRess[resources.length] = resource;

					resources = newRess;
				}
			}
			else return;

			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream writeOut = new DataOutputStream(out);

				for(int i= 0; i < resources.length; i++){

					//write type 
					byte[] buffer2 = resources[i].getClass().toString().getBytes();
					writeOut.writeInt(buffer2.length);
					writeOut.write(buffer2);
					
					IEntity res = (IEntity)resources[i];
					//write uri
					byte[] buffer0 = res.getUri().toString().getBytes();
					writeOut.writeInt(buffer0.length);
					writeOut.write(buffer0);

					//write oid 
					writeOut.writeLong(res.getOid());

				}
				byte[] buffer = out.toByteArray();
				writeOut.close();

				super.javaToNative(buffer, transferData);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	public Object nativeToJava(TransferData transferData){
		if (isSupportedType(transferData)) {

			byte[] buffer = (byte[])super.nativeToJava(transferData);
			if (buffer == null) return null;

			IResource[] resources = new IResource[0];
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);
				while(readIn.available() > 20) {

					//read type
					int size1 = readIn.readInt();
					byte[] type = new byte[size1];
					readIn.read(type);
					String typeAsStr = new String(type);
					
					//read uri
					int size = readIn.readInt();
					byte[] uribytes = new byte[size];
					readIn.read(uribytes);
					String uri = new String(uribytes);

					//read oid 
					long oid = readIn.readLong();

					IResource res = null; 
					if (typeAsStr.equals(NamedConcept.class.toString()) ){
						res = new NamedConcept(uri);
						((NamedConcept)res).setOid(oid);

					}
					else if (typeAsStr.equals(DataProperty.class.toString()) ){ 
						res = new DataProperty(uri);
						((Property)res).setOid(oid);
					}
					else if (typeAsStr.equals(ObjectProperty.class.toString()) ){ 
						res = new ObjectProperty(uri);
						((Property)res).setOid(oid);
					}
					else if (typeAsStr.equals(NamedIndividual.class.toString()) ) {
						res = new NamedIndividual(uri);
						((NamedIndividual)res).setOid(oid);
					}

					if (res != null){
						IResource[] newRess = new IResource[resources.length + 1];
						System.arraycopy(resources, 0, newRess, 0, resources.length);
						newRess[resources.length] = res;

						resources = newRess;
					}
				}
				readIn.close();
			} catch (IOException ex) {
				return null;
			}
			return resources;
		}
		return null;
	}
}