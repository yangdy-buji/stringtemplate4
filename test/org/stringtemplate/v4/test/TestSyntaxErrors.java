/*
 [The "BSD licence"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.stringtemplate.v4.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.stringtemplate.*;
import org.stringtemplate.v4.misc.*;
import org.stringtemplate.v4.compiler.STException;
import org.antlr.runtime.RecognitionException;

public class TestSyntaxErrors extends BaseTest {
    @Test public void testEmptyExpr() throws Exception {
        String template = " <> ";
        STGroup group = new STGroup();
		String result = null;
		try {
        	group.defineTemplate("test", template);
		}
		catch (STException se) {
            RecognitionException re = (RecognitionException)se.getCause();
            result = new STCompiletimeMessage(ErrorType.SYNTAX_ERROR,
                                              re.token.getInputStream().getSourceName(),
                                              re.token,re,se.getMessage()).toString();
		}
        String expected = "1:0: this doesn't look like a template: \" <> \"";
        assertEquals(expected, result);
    }

    @Test public void testEmptyExpr2() throws Exception {
        String template = "hi <> ";
        STGroup group = new STGroup();
		String result = null;
		try {
        	group.defineTemplate("test", template);
		}
		catch (STException se) {
            RecognitionException re = (RecognitionException)se.getCause();
            result = new STCompiletimeMessage(ErrorType.SYNTAX_ERROR,
                                              re.token.getInputStream().getSourceName(),
                                              re.token,re,se.getMessage()).toString();
		}
        String expected = "1:3: doesn't look like an expression";
        assertEquals(expected, result);
    }

    @Test public void testWeirdChar() throws Exception {
        String template = "   <*>";
        STGroup group = new STGroup();
		String result = null;
		try {
        	group.defineTemplate("test", template);
		}
		catch (STException se) {
            RecognitionException re = (RecognitionException)se.getCause();
            result = new STCompiletimeMessage(ErrorType.SYNTAX_ERROR,
                                              null,
                                              re.token,re,se.getMessage()).toString();
		}
        String expected = "1:4: invalid character: *";
        assertEquals(expected, result);
    }

    @Test public void testValidButOutOfPlaceChar() throws Exception {
        String templates =
            "foo() ::= <<hi <.> mom>>\n";
        writeFile(tmpdir, "t.stg", templates);

		STErrorListener errors = new ErrorBuffer();
		STGroupFile group = new STGroupFile(tmpdir+"/"+"t.stg");
		ErrorManager.setErrorListener(errors);
		group.load(); // force load
        String expected = "t.stg 1:15: doesn't look like an expression"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testValidButOutOfPlaceCharOnDifferentLine() throws Exception {
        String templates =
				"foo() ::= \"hi <\n" +
				".> mom\"\n";
		writeFile(tmpdir, "t.stg", templates);

		ErrorBuffer errors = new ErrorBuffer();
		STGroupFile group = new STGroupFile(tmpdir+"/"+"t.stg");
		ErrorManager.setErrorListener(errors);
		group.load(); // force load
		String expected = "[t.stg 1:15: \\n in string, t.stg 1:14: doesn't look like an expression]";
		String result = errors.errors.toString();
		assertEquals(expected, result);
	}

    @Test public void testErrorInNestedTemplate() throws Exception {
        String templates =
            "foo() ::= \"hi <name:{[<aaa.bb!>]}> mom\"\n";
        writeFile(tmpdir, "t.stg", templates);

		STGroupFile group = null;
		STErrorListener errors = new ErrorBuffer();
		group = new STGroupFile(tmpdir+"/"+"t.stg");
		ErrorManager.setErrorListener(errors);
		group.load(); // force load
		String expected = "t.stg 1:29: '!' came as a complete surprise to me"+newline;
		String result = errors.toString();
		assertEquals(expected, result);
	}

    @Test public void testEOFInExpr() throws Exception {
        String templates =
            "foo() ::= \"hi <name:{[<aaa.bb>]}\"\n";
        writeFile(tmpdir, "t.stg", templates);

		STGroupFile group = null;
		STErrorListener errors = new ErrorBuffer();
		group = new STGroupFile(tmpdir+"/"+"t.stg");
		ErrorManager.setErrorListener(errors);
		group.load(); // force load
		String expected = "t.stg 1:32: premature EOF"+newline;
		String result = errors.toString();
		assertEquals(expected, result);
	}

    @Test public void testMissingRPAREN() throws Exception {
        String templates =
            "foo() ::= \"hi <foo(>\"\n";
        writeFile(tmpdir, "t.stg", templates);

		STGroupFile group = null;
		STErrorListener errors = new ErrorBuffer();
		group = new STGroupFile(tmpdir+"/"+"t.stg");
		ErrorManager.setErrorListener(errors);
		group.load(); // force load
		String expected = "t.stg 1:19: mismatched input '>' expecting RPAREN"+newline;
		String result = errors.toString();
		assertEquals(expected, result);
	}

}