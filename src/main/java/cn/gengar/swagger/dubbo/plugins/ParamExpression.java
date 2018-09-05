package cn.gengar.swagger.dubbo.plugins;

import org.springframework.web.servlet.mvc.condition.NameValueExpression;

/**
 * @author gengar yu
 */

public class ParamExpression implements NameValueExpression<String> {

    private String name;
    private String value;
    private boolean negated;

    public ParamExpression(String name, String value) {
        this.name = name;
        this.value = value;
        this.negated = false;
    }

    public ParamExpression(String name, String value, boolean negated) {
        this.name = name;
        this.value = value;
        this.negated = negated;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }
}
