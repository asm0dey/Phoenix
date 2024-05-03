package tech.petrepopescu.phoenix.parser.elements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.petrepopescu.phoenix.parser.ElementFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class CommentElementTests {
    @Test
    void simpleTest() {
        String line = "@* test comment *@";
        CommentElement element = new CommentElement(List.of(line), 0, new ElementFactory(Set.of()), ElementFactory.DEFAULT_BUILDER_NAME);
        element.parse("");

        Assertions.assertEquals(0, element.write().toString().length());
    }

    @Test
    void testMultiLineComment() {
        List<String> lines = new ArrayList<>();
        lines.add("@*");
        lines.add("This is a comment");
        lines.add("on multiple lines");
        lines.add("*@");

        CommentElement element = new CommentElement(lines, 0, new ElementFactory(Set.of()), ElementFactory.DEFAULT_BUILDER_NAME);
        element.parse("");

        Assertions.assertEquals(0, element.write().toString().length());
    }

    @Test
    void testCommentInsideHtml() {
        String line = "<a href=simpleTest.html @*a simple link *@>Test link</a>";
        HtmlElement element = new HtmlElement(List.of(line), 0, new ElementFactory(Set.of()), ElementFactory.DEFAULT_BUILDER_NAME);
        element.parse("");

        String expected = "\t\tcontentBuilder.append(\"<a href=simpleTest.html \");\n" +
                "\t\tcontentBuilder.append(\">Test link</a>\");\n" +
                "\t\tcontentBuilder.append(\"\\n\");\n";
        Assertions.assertEquals(expected, element.write().toString());
    }
}
