package kr.gdg.gcplab.example01;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.JsonObject;

public class JangEServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("idStr");
        String passord = req.getParameter("password");
        
        PersistenceManager pm = PMF.get().getPersistenceManager();
        
        JsonObject retObj = new JsonObject();
        
        try{
            JangE jange = pm.getObjectById(JangE.class, KeyFactory.createKey(JangE.class.getSimpleName(), idStr));
            if(jange != null && jange.getPassword().equals(passord)){
                retObj.addProperty("result", "success");
                retObj.addProperty("idStr", jange.keyName());
                retObj.addProperty("encKey", jange.encodedKey());
                
                System.out.println("logged In->["+jange.keyName()+"]["+jange.encodedKey()+"]");
            }
        }catch(Exception e){
            
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

}
