/**
 * @author XiongJie, Date: 13-12-27
 */
package spring.test.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** The user in component-2 */
@Component
public class User {
    @Autowired Provider provider;

    public String work(){
        return provider.doSomeFavor(this);
    }
}
