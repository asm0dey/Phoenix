package tech.petrepopescu.phoenix.parser.elements;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import tech.petrepopescu.phoenix.exception.ParsingException;
import tech.petrepopescu.phoenix.parser.ElementFactory;
import tech.petrepopescu.phoenix.parser.VariableRegistry;

import java.util.ArrayList;
import java.util.List;

public class ConstructorElement extends Element {
    private List<Pair<String, String>> parameters = new ArrayList<>();

    private String className = null;

    public ConstructorElement(List<String> lines, int lineIndex, ElementFactory elementFactory, String builderName) {
        super(lines, lineIndex, elementFactory, builderName);
    }

    @Override
    public int parse(String fileName) {
        String line = StringUtils.substring(StringUtils.replace(StringUtils.trim(lines.get(this.lineNumber)), "@args", ""), 0, -1);
        line = StringUtils.substring(StringUtils.trim(line), 1);
        parameters = splitIntoVariableDeclarations(line);
        for (Pair<String, String> param:parameters) {
            VariableRegistry.getInstance().add(fileName, param.getRight(), param.getLeft());
        }
        return this.lineNumber;
    }

    private List<Pair<String, String>> splitIntoVariableDeclarations(String line) {
        List<Pair<String, String>> variables = new ArrayList<>();
        String[] words = StringUtils.split(line, " ");
        StringBuilder declarationBuilder = new StringBuilder();
        for (int count = 0; count < words.length; count++) {
            String word = words[count];
            int numLeftGeneric = StringUtils.countMatches(word, '<');
            if (numLeftGeneric == 0) {
                variables.add(Pair.of(word, toVarName(words[count+1])));
                count++;
            } else {
                int numRightGeneric = StringUtils.countMatches(word, '>');
                if (numLeftGeneric == numRightGeneric) {
                    variables.add(Pair.of(word, toVarName(words[count+1])));
                    count++;
                } else {
                    declarationBuilder.setLength(0);
                    declarationBuilder.append(word);
                    count++;
                    for(; count < words.length; count++) {
                        word = words[count];
                        declarationBuilder.append(" ").append(word);
                        numLeftGeneric += StringUtils.countMatches(word, '<');
                        numRightGeneric += StringUtils.countMatches(word, '>');
                        if (numLeftGeneric == numRightGeneric) {
                            variables.add(Pair.of(declarationBuilder.toString(), toVarName(words[count+1])));
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return variables;
    }

    private String toVarName(String varName) {
        if (StringUtils.endsWith(varName, ",")) {
            return StringUtils.substring(varName, 0, varName.length() - 1);
        }
        return varName;
    }

    @Override
    public StringBuilder write() {
        if (StringUtils.isBlank(className)) {
            throw new ParsingException("No class name provided");
        }
        this.contentBuilder.append("public final class ").append(className).append(" extends HtmlFormat {\n");
        for (Pair<String, String> param:parameters) {
            appendVariableDeclaration(param);
        }
        this.contentBuilder.append("\n");
        this.contentBuilder.append("\t").append("private ").append(className).append("(");
        appendConstructorParams();
        this.contentBuilder.append(") {\n");
        for (Pair<String, String> param:parameters) {
            appendVariableInitialisation(param);
        }
        this.contentBuilder.append("\t}\n");

        this.contentBuilder.append("\tpublic static Format render(");
        appendConstructorParams();
        this.contentBuilder.append(") {\n");
        this.contentBuilder.append("\t\t").append("return new ").append(this.className).append("(");
        appendVariableCall();
        this.contentBuilder.append(");\n");
        this.contentBuilder.append("\t}\n");
        return this.contentBuilder;
    }

    private void appendConstructorParams() {
        final StringBuilder constructorParamsBuilder = new StringBuilder();
        for (Pair<String, String> param:parameters) {
            if (!StringUtils.isEmpty(constructorParamsBuilder)) {
                constructorParamsBuilder.append(", ");
            }
            constructorParamsBuilder.append(param.getLeft()).append(" ").append(param.getRight());
        }
        this.contentBuilder.append(constructorParamsBuilder);
    }

    private void appendVariableDeclaration(Pair<String, String> variable) {
        this.contentBuilder.append("\t").append("private final ").append(variable.getLeft())
                .append(" ").append(variable.getRight()).append(";\n");
    }

    private void appendVariableInitialisation(Pair<String, String> param) {
        this.contentBuilder.append("\t\t").append("this.").append(param.getRight())
                .append(" = ").append(param.getRight()).append(";\n");
    }

    private void appendVariableCall() {
        final StringBuilder builder = new StringBuilder();
        for (Pair<String, String> param:this.parameters) {
            if (!StringUtils.isEmpty(builder)) {
                builder.append(", ");
            }
            builder.append(param.getRight());
        }

        this.contentBuilder.append(builder);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
