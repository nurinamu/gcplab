package kr.gdg.gcplab.example01;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable
public class JangE {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String encodedKey;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.pk-name", value="true")
    private String keyName;
    
    @Persistent
    private String password;
    
    public JangE(String idStr){
        encodedKey = KeyFactory.createKeyString(JangE.class.getSimpleName(), idStr);
    }

    public String getPassword() {
        if(password == null) return "";
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String keyName(){
        return keyName;
    }
    
    public String encodedKey(){
        return encodedKey;
    }
}
