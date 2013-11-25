package kr.gdg.gcplab.example01;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NakSeoServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String encKey = req.getParameter("encKey");
        String idx = req.getParameter("idx");

        JsonObject retObj = new JsonObject();
        
        if(encKey != null && idx != null){
        	if(DatastoreType.currentMode() == DatastoreType.USE_DATASTORE){
            	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            	try {
    				Entity entity = ds.get(KeyFactory.stringToKey(idx));
    				if(KeyFactory.keyToString(entity.getKey().getParent()).equals(encKey)){
    					BlobKey blobKey = (BlobKey)entity.getProperty("blobKey");
    					if(blobKey != null){
    						BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
    					}
    					ds.delete(KeyFactory.stringToKey(idx));
    					retObj.addProperty("result", "success");
    				}
    			} catch (EntityNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }else if(DatastoreType.currentMode() == DatastoreType.USE_PERSISTENT){
            	PersistenceManager pm = PMF.get().getPersistenceManager();
            }
        }
        
        if(retObj.get("result") == null){
        	retObj.addProperty("result", "fail");
        }
        
        resp.getWriter().write(retObj.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String encKey = req.getParameter("encKey");
        String fetch = req.getParameter("fetch");
        String nextToken = req.getParameter("nextToken");
        
        JsonObject retObj = new JsonObject();
        
        if(DatastoreType.currentMode() == DatastoreType.USE_DATASTORE){
        	
        	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        	
        	com.google.appengine.api.datastore.Query fetchQuery = new com.google.appengine.api.datastore.Query(NakSeo.class.getSimpleName());
        	if(encKey != null){
        		if(fetch != null && fetch.trim().equals("mine")){
        			fetchQuery.setAncestor(KeyFactory.stringToKey(encKey));
        		}else{
        			fetchQuery.setAncestor(JangE.PARENT_JANGE);
        		}
        	}
        	
        	FetchOptions fetOpt = null;
        	if(nextToken != null){
        		fetOpt = FetchOptions.Builder.withStartCursor(Cursor.fromWebSafeString(nextToken));
        	}else{
        		fetOpt = FetchOptions.Builder.withDefaults();
        	}
        	
        	PreparedQuery pq = ds.prepare(fetchQuery);
        	
        	if(fetOpt != null){
        		QueryResultList<Entity> results = pq.asQueryResultList(fetOpt);
        		
        		if(results != null){
                    JsonArray nakseos = new JsonArray();
                    JsonObject tmpNakSeo = null;
                    for(Entity entity : results){
                        tmpNakSeo = new JsonObject();
                        tmpNakSeo.addProperty("idx", KeyFactory.keyToString(entity.getKey()));
                        tmpNakSeo.addProperty("owner", entity.getKey().getParent().getName());
                        tmpNakSeo.addProperty("encOwnerKey", KeyFactory.keyToString(entity.getKey().getParent()));
                        tmpNakSeo.addProperty("content", (String)entity.getProperty("content"));
                        try{
                            tmpNakSeo.addProperty("blobKey", ((BlobKey)entity.getProperty("blobKey")).getKeyString());
                        }catch(Exception e){}
                        tmpNakSeo.addProperty("regDate", ((Date)entity.getProperty("registerDate")).toString());
                        nakseos.add(tmpNakSeo);
                    }
                    
                    retObj.add("nakseo", nakseos);
                    
                    if(results.size() < 1){
                        retObj.addProperty("result","no_more");
                    }else{
                        String newToken = results.getCursor().toWebSafeString();
                        retObj.addProperty("next", newToken);
                        retObj.addProperty("result", "success");
                    }
                    
                }
        	}
        	
        	
        	
        	
        }else if(DatastoreType.currentMode() == DatastoreType.USE_PERSISTENT){
        	PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                Query q = pm.newQuery(NakSeo.class);
                q.setOrdering("registerDate desc");
                if(nextToken != null && nextToken.trim().length() > 0){
                    Cursor cursor = Cursor.fromWebSafeString(nextToken);
                    Map<String, Object> extensionMap = new HashMap<String, Object>();
                    extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
                    q.setExtensions(extensionMap);
                }
                q.setRange(0, 20);
                
                List<NakSeo> results = null;
                if(fetch != null && fetch.equals("mine") && encKey != null && fetch.trim().length()>0){
                    q.setFilter("owner == ownerEncKey");
                    q.declareParameters("String ownerEncKey");
                    results = (List<NakSeo>)q.execute(encKey);
                    System.out.println("fetch by key->"+KeyFactory.stringToKey(encKey).getName());
                }else{
                    results = (List<NakSeo>)q.execute();
                    System.out.println("fetch all");
                }
                
                if(results != null){
                    JsonArray nakseos = new JsonArray();
                    JsonObject tmpNakSeo = null;
                    for(NakSeo nakseo : results){
                        tmpNakSeo = new JsonObject();
                        tmpNakSeo.addProperty("idx", nakseo.getKeyString());
                        tmpNakSeo.addProperty("owner", KeyFactory.stringToKey(nakseo.owner()).getName());
                        tmpNakSeo.addProperty("encOwnerKey", nakseo.owner());
                        tmpNakSeo.addProperty("content", nakseo.getContent().getValue());
                        try{
                            tmpNakSeo.addProperty("blobKey", nakseo.getBlobKey().getKeyString());
                        }catch(Exception e){}
                        tmpNakSeo.addProperty("regDate", nakseo.getRegDate().toString());
                        nakseos.add(tmpNakSeo);
                    }
                    
                    retObj.add("nakseo", nakseos);
                    
                    if(results.size() < 1){
                        retObj.addProperty("result","no_more");
                    }else{
                        String newToken = JDOCursorHelper.getCursor(results).toWebSafeString();
                        retObj.addProperty("next", newToken);
                        retObj.addProperty("result", "success");
                    }
                    
                }
                
                
            }finally{
                if(pm != null){
                    pm.close();
                }
            }
        }
        
        
        if(retObj.get("result") == null){
            retObj.addProperty("result", "fail");
        }
        
        resp.getWriter().write(retObj.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String encKey = req.getParameter("encKey");
        String content = req.getParameter("content");
        String blobKeyStr = req.getParameter("blobKey");
        String regDate = req.getParameter("regDate");
        BlobKey blobKey = null;

        JsonObject retObj = new JsonObject();
        
        if(encKey != null && content != null){
        	
        	if(DatastoreType.currentMode() == DatastoreType.USE_DATASTORE){
        		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        		
        		Transaction tx = ds.beginTransaction();
        		Entity newEntity = new Entity(KeyFactory.createKey(KeyFactory.stringToKey(encKey), NakSeo.class.getSimpleName(), System.currentTimeMillis()));
        		newEntity.setProperty("content", content);
        		if(blobKeyStr != null && blobKeyStr.trim().length() > 0){
        			newEntity.setProperty("blobKey", KeyFactory.stringToKey(blobKeyStr));
        		}
        		
        		Calendar cal = Calendar.getInstance();
        		if(regDate != null){
        			cal.setTimeInMillis(Long.parseLong(regDate));
        			newEntity.setProperty("registerDate", cal.getTime());
        		}else{
        			cal.setTimeInMillis(System.currentTimeMillis());
        			newEntity.setProperty("registerDate", cal.getTime());
        		}
        		
        		ds.put(tx, newEntity);
        		try {
					tx.commitAsync().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		retObj.addProperty("result", "success");
        		
            }else if(DatastoreType.currentMode() == DatastoreType.USE_PERSISTENT){
            	PersistenceManager pm = PMF.get().getPersistenceManager();
                try{
                    if(blobKeyStr != null && blobKeyStr.trim().length() > 0){
                        blobKey = new BlobKey(blobKeyStr);
                    }
                    
                    NakSeo newNakSeo = new NakSeo(encKey, content, blobKey);
                    newNakSeo = pm.makePersistent(newNakSeo);
                    retObj.addProperty("result", "success");
                }finally{
                    if(pm != null){
                        pm.close();
                    }
                }
            }
            
        }
        
        if(retObj.get("result") == null){
            retObj.addProperty("result", "fail");
        }
        
        resp.getWriter().write(retObj.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

}
