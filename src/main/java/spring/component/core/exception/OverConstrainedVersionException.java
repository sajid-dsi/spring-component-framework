/**
 * @author XiongJie, Date: 13-9-3
 */
package spring.component.core.exception;

import spring.component.core.Component;
import spring.component.core.ComponentException;

/**
 * 当VersionRange无法匹配出目标版本时抛出该异常
 * 从Maven中copy过来
 */
public class OverConstrainedVersionException extends ComponentException {
    public OverConstrainedVersionException(String s, Component component) {
        super(s);
        this.component = component;
    }
}
