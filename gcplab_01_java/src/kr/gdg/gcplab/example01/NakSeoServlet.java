package kr.gdg.gcplab.example01;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.KeyFactory;
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
        
        PersistenceManager pm = PMF.get().getPersistenceManager();
        
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String encKey = req.getParameter("encKey");
        String fetch = req.getParameter("fetch");
        String nextToken = req.getParameter("nextToken");
        
        PersistenceManager pm = PMF.get().getPersistenceManager();
        JsonObject retObj = new JsonObject();
        
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
        BlobKey blobKey = null;

        JsonObject retObj = new JsonObject();
        
        if(encKey != null && content != null){
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
        
        if(retObj.get("result") == null){
            retObj.addProperty("result", "fail");
        }
        
        resp.getWriter().write(retObj.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

}
