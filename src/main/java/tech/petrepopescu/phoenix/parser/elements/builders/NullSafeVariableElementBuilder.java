package tech.petrepopescu.phoenix.parser.elements.builders;

import tech.petrepopescu.phoenix.utils.StringUtils;
import org.springframework.stereotype.Component;
import tech.petrepopescu.phoenix.parser.ElementFactory;
import tech.petrepopescu.phoenix.parser.elements.Element;
import tech.petrepopescu.phoenix.parser.elements.NullSafeVariableElement;

import java.util.List;

@Component
public class NullSafeVariableElementBuilder extends ElementBuilder {
    @Override
    public int order() {
        // Moved priority down so that we guarantee that `@?raw()` is first
        return 100;
    }

    @Override
    public boolean isValid(String line) {
        return StringUtils.startsWith(line, "@?");
    }

    @Override
    public Element buildFromLine(List<String> lines, int lineNumber, ElementFactory elementFactory, String builderName) {
        return new NullSafeVariableElement(lines, lineNumber, elementFactory, builderName);
    }
}
