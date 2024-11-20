package tech.petrepopescu.phoenix.parser.elements;

import org.apache.commons.lang3.StringUtils;
import tech.petrepopescu.phoenix.parser.ElementFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerElement extends Element {
    final List<Element> nestedElements = new ArrayList<>();
    protected AbstractContainerElement(List<String> lines, int lineIndex, ElementFactory elementFactory, String builderName) {
        super(lines, lineIndex, elementFactory, builderName);
    }

    protected void parseContentInside(String line, String fileName) {
        int indexOfStatementEnd = indexOfStatementEnd(line);
        if (indexOfStatementEnd == -1) {
            // The nested statement does not end on the same line
            this.lineNumber++;
            line = StringUtils.trim(this.lines.get(this.lineNumber));
            while (!StringUtils.startsWith(line, "}")) {
                Element subElement = elementFactory.getElement(lines, lineNumber);
                subElement.tabs(this.numTabs + 1);
                nestedElements.add(subElement);
                this.lineNumber = subElement.parse(fileName) + 1;
                line = StringUtils.trim(this.lines.get(this.lineNumber));
            }
            if (line.length() > 1) {
                this.lineNumber = discoverNextElement(line, lines, this.lineNumber, fileName);
            }
        } else {
            // The IF statement end on the same line. Extract the content
            int indexOfContentStart = this.indexOfContentStart(line);
            String content = StringUtils.substring(line, indexOfContentStart + 1, indexOfStatementEnd);
            Element subElement = elementFactory.getElement(content);
            subElement.tabs(this.numTabs + 1);
            subElement.parse(fileName);
            nestedElements.add(subElement);
            if (indexOfStatementEnd + 1 < line.length()) {
                discoverNextElement(StringUtils.substring(line, indexOfStatementEnd + 1), fileName);
            }
        }
    }

    private int indexOfStatementEnd(String line) {
        return StringUtils.indexOf(line, '}');
    }

    private int indexOfContentStart(String line) {
        return StringUtils.indexOf(line, '{');
    }

    @Override
    public void setBuilderName(String builderName) {
        this.builderName = builderName;
        for (Element nestedElement : nestedElements) {
            nestedElement.setBuilderName(builderName);
        }
    }
}
