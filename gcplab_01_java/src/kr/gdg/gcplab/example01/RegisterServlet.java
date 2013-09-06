package kr.gdg.gcplab.example01;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class RegisterServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/page/register.html");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = (String)req.getParameter("idStr");
        String password = (String)req.getParameter("password");
        
        System.out.println("ID["+idStr+"]PWD["+password+"]");
        
        JsonObject retJson = new JsonObject();
        
        if(idStr != null && password != null){
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                JangE newJangE = new JangE(idStr);
                newJangE.setPassword(password);
                newJangE = pm.makePersistent(newJangE);
                
                retJson.addProperty("result", "success");
                retJson.addProperty("idStr", newJangE.keyName());
                retJson.addProperty("encKey", newJangE.encodedKey());
            }finally{
                if(pm != null){
                    pm.close();
                }
            }
        }else{
            retJson.addProperty("result","fail");
        }
        
        resp.getWriter().write(retJson.toString());
    }

}
