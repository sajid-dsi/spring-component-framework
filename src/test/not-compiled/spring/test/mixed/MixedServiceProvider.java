/**
 * @author XiongJie, Date: 13-10-30
 */
package net.happyonroad.spring.test.mixed;

import net.happyonroad.spring.test.api.ServiceProvider;
import org.springframework.stereotype.Component;

/** 独立的服务提供者 */
@Component
public class MixedServiceProvider implements ServiceProvider {
    @Override
    public String provide(String userName) {
        return "[ " + userName + " ] message by mixed";
    }
}
