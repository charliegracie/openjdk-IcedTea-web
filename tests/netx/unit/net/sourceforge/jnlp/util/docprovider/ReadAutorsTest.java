/* ReadAutorsTest.java
   Copyright (C) 2017 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp.util.docprovider;

import java.io.IOException;
import java.io.StringReader;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.HtmlFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ManFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ReadAutorsTest {
    
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String HTMLNEWLINE = "<BR/>";
    public static final String MANNEWLINE = ".br";
    
    public static final String TESTTEXT = "<test@test.test><<>><><>" + NEWLINE + "<><><>";
    public static final String BRACKETSTESTREPLACEDHTML = "&#60test@test.test&#62&#60&#60&#62&#62&#60&#62&#60&#62" + HTMLNEWLINE + NEWLINE + "&#60&#62&#60&#62&#60&#62" + HTMLNEWLINE + NEWLINE;
    public static final String BRACKETSTESTREPLACEDMAN = "<test@test.test><<>><><>" + NEWLINE + MANNEWLINE + NEWLINE + "<><><>" + NEWLINE + MANNEWLINE + NEWLINE;
    public static final String BRACKETSTESTREPLACEDPLAIN = TESTTEXT + NEWLINE;
    
    public static final String FILESTREAM = "RANDOM TEXT" + NEWLINE + "RANDOM TEXT" + NEWLINE
            + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + NEWLINE 
            + "MORE RANDOM TEXT" + NEWLINE
            + "MORE RANDOM TEXT" + NEWLINE
            + "MORE RANDOM TEXT" + NEWLINE;
    public static final String FILESTREAMHTML = "RANDOM TEXT" + HTMLNEWLINE + NEWLINE + "RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "ANOTHER RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + HTMLNEWLINE + NEWLINE
            + "MORE RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "MORE RANDOM TEXT" + HTMLNEWLINE + NEWLINE
            + "MORE RANDOM TEXT" + HTMLNEWLINE + NEWLINE;
    public static final String FILESTREAMPLAIN = "RANDOM TEXT" + NEWLINE + "RANDOM TEXT" + NEWLINE
            + "ANOTHER RANDOM TEXT"
            + "ANOTHER RANDOM TEXT"
            + "ANOTHER RANDOM TEXT"
            + "ANOTHER RANDOM TEXT"
            + "ANOTHER RANDOM TEXT"
            + "ANOTHER RANDOM TEXT" + NEWLINE
            + "MORE RANDOM TEXT" + NEWLINE
            + "MORE RANDOM TEXT" + NEWLINE
            + "MORE RANDOM TEXT" + NEWLINE;

    @Test
    public void replaceBracketsWithEntitiesHtml() throws IOException {
        TextsProvider tp = new TextsProvider("utf-8", new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl(new StringReader(TESTTEXT));
        assertEquals(BRACKETSTESTREPLACEDHTML, output);
    }

    @Test
    public void replaceBracketsWithEntitiesMan() throws IOException {
        TextsProvider tp = new TextsProvider("utf-8", new ManFormatter(), true, true) {
            @Override
            public String getId() {
                return "test2";
            }
        };
        String output = tp.readAuthorsImpl(new StringReader(TESTTEXT));
        assertEquals(BRACKETSTESTREPLACEDMAN, output);
    }

    @Test
    public void replaceBracketsWithEntitiesPlain() throws IOException {
        TextsProvider tp = new TextsProvider("utf-8", new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test3";
            }
        };
        String output = tp.readAuthorsImpl(new StringReader(TESTTEXT));
        assertEquals(BRACKETSTESTREPLACEDPLAIN, output);
    }

    @Test
    public void newLineTestHtml() throws IOException {
        TextsProvider tp = new TextsProvider("utf-8", new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test4";
            }
        };
        String output = tp.readAuthorsImpl(new StringReader(FILESTREAM));
        assertEquals(FILESTREAMHTML, output);

    }
    
    @Test
    public void newLineTestPlainText() throws IOException {
        TextsProvider tp = new TextsProvider("utf-8", new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test5";
            }
        };
        String output = tp.readAuthorsImpl(new StringReader(FILESTREAM));
        assertEquals(FILESTREAMPLAIN, output);

    }
}