package kr.gdg.gcplab.example01;

public class DatastoreType {
	public static final int USE_PERSISTENT = 100;
	public static final int USE_DATASTORE = 200;
	
	private static int mode = USE_DATASTORE;
	
	public static int currentMode(){
		return mode;
	}
}
