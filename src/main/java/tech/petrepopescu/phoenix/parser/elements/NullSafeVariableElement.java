package tech.petrepopescu.phoenix.parser.elements;

import org.apache.commons.lang3.StringUtils;
import tech.petrepopescu.phoenix.parser.ElementFactory;

import java.util.List;
import java.util.UUID;

public class NullSafeVariableElement extends Element {
    public NullSafeVariableElement(List<String> lines, int lineIndex, ElementFactory elementFactory, String builderName) {
        super(lines, lineIndex, elementFactory, builderName);
    }

    @Override
    public int parse(String fileName) {
        String line = lines.get(lineNumber).trim();
        int elementEnd = indexOfElementEnd(line, 0);
        String variableExpression = StringUtils.substring(line, 2, elementEnd);
        VariableElement variableElement = new VariableElement(List.of("@" + variableExpression), 0, elementFactory, builderName);
        variableElement.tabs(this.numTabs + 1);
        variableElement.parse(fileName);
        String varName = "tmpVar_" + StringUtils.remove(UUID.randomUUID().toString(), '-');
        appendAsCode("try {\n");
        appendAsCode("var " + varName + " = " + variableElement.getEvalExpression() + ";\n", this.numTabs + 1);
        appendAsCode("if (!" + varName + ".equals(\"null\")) {\n", this.numTabs + 1);
        appendWithContentBuilder(varName, this.numTabs + 2);
        appendAsCode("}\n", this.numTabs + 1);
        appendAsCode("} catch (NullPointerException e) {}\n");
        discoverNextElement(StringUtils.substring(line, elementEnd), fileName);
        return this.lineNumber;
    }

    @Override
    public StringBuilder write() {
        if (this.nextElement != null) {
            this.contentBuilder.append(this.nextElement.write());
        }
        return this.contentBuilder;
    }
}
