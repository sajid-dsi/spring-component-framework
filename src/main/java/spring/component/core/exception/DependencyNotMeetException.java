/**
 * @author XiongJie, Date: 13-8-29
 */
package spring.component.core.exception;

import spring.component.core.Component;
import spring.component.core.ComponentException;
import spring.component.core.support.Dependency;

/**
 * 组件依赖未满足
 */
public class DependencyNotMeetException extends ComponentException {
    private final Dependency dependency;
    private final Component  component;

    public DependencyNotMeetException(Dependency dependency, String reason) {
        super("Can't find " + dependency + " " + reason);
        this.dependency = dependency;
        this.component = null;
    }

    public DependencyNotMeetException(Dependency dependency, Component component, Throwable cause) {
        super("Can't find " + dependency + " for " + component, cause);
        this.dependency = dependency;
        this.component = component;
    }

    public DependencyNotMeetException(Component component, String message, DependencyNotMeetException cause) {
        super(message, cause);
        this.dependency = cause.getDependency();
        this.component = component;
    }

    public Dependency getDependency() {
        return dependency;
    }
}
