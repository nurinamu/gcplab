package kr.gdg.gcplab.example01;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.gson.JsonObject;

public class ImageServlet extends HttpServlet {

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String blobKey = req.getParameter("blobKey");
        JsonObject retObj = new JsonObject();
        
        if(blobKey != null){
            BlobstoreServiceFactory.getBlobstoreService().delete(new BlobKey(blobKey));
            retObj.addProperty("result","success");
        }
        
        if (retObj.get("result") == null) {
            retObj.addProperty("result", "fail");
        }

        resp.getWriter().write(retObj.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String blobKey = req.getParameter("blobKey");   
        String path = req.getParameter("path");
        
        if(path != null && path.trim().length() > 0){
            JsonObject retObj = new JsonObject();
            
            String uploadBlobPath = BlobstoreServiceFactory.getBlobstoreService().createUploadUrl(URLDecoder.decode(path), UploadOptions.Builder.withDefaults());
            
            retObj.addProperty("result", "success");
            retObj.addProperty("path", URLEncoder.encode(uploadBlobPath));
            
            if (retObj.get("result") == null) {
                retObj.addProperty("result", "fail");
            }

            resp.getWriter().write(retObj.toString());
        }else if(blobKey != null){
            BlobstoreServiceFactory.getBlobstoreService().serve(new BlobKey(blobKey), resp);  
        }
        
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = bs.getUploads(req);
        List<BlobKey> imageBlobs = blobs.get("source");
        
        JsonObject retObj = new JsonObject();
        
        if(imageBlobs.size() > 0){
            BlobKey image = imageBlobs.get(0);
            retObj.addProperty("result", "success");
            retObj.addProperty("blobKey", image.getKeyString());
        }
        
        if (retObj.get("result") == null) {
            retObj.addProperty("result", "fail");
        }

        resp.getWriter().write(retObj.toString());
    }

}
