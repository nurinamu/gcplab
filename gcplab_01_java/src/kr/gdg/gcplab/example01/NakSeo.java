package kr.gdg.gcplab.example01;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;


@PersistenceCapable
public class NakSeo {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    
    public NakSeo(String owner_, String content_, BlobKey blobKey_){
        registerDate = new GregorianCalendar().getTime();
        content = new Text(content_);
        owner = owner_;
        blobKey = blobKey_;
    }
    
    public String getKeyString(){
        return KeyFactory.keyToString(key);
    }
    
    @Persistent
    private Text content;
    
    @Persistent
    private Date registerDate;
    
    @Persistent
    private String owner;
    
    @Persistent
    private BlobKey blobKey;
    
    public String owner(){
        return owner;
    }
    
    public Text getContent(){
        return content;
    }
    
    public Date getRegDate(){
        return registerDate;
    }
    
    public BlobKey getBlobKey(){
        return blobKey;
    }
}
