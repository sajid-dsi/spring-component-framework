/**
 * @author XiongJie, Date: 13-10-30
 */
package net.happyonroad.spring.test.mixed;

import net.happyonroad.spring.test.api.ServiceProvider;
import net.happyonroad.spring.test.api.ServiceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/** 独立app里面使用的类 */
@Component
public class MixedServiceUser implements ServiceUser {
    @Autowired
    private List<ServiceProvider> providers;

    @Override
    public String work() {
        StringBuilder builder = new StringBuilder();
        for (ServiceProvider provider : providers) {
            String msg = provider.provide("MixedServiceUser");
            System.out.println(msg);
            builder.append(msg).append("\n");
        }
        return builder.toString();
    }
}
