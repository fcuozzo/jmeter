/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 */

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.


package org.htmlparser;

//////////////////
// Java Imports //
//////////////////
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.htmlparser.parserHelper.StringParser;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * NodeReader builds on the BufferedReader, providing methods to read one element
 * at a time
 */
public class NodeReader extends BufferedReader
{
    public static final String DECIPHER_ERROR =
        "NodeReader.readElement() : Error occurred while trying to decipher the tag using scanners";
    protected int posInLine = -1;
    protected String line;
    protected Node node = null;
    protected TagScanner previousOpenScanner = null;
    protected String url;
    private Parser parser;
    private int lineCount;
    private String previousLine;
    private StringParser stringParser = new StringParser();
    private RemarkNodeParser remarkNodeParser = new RemarkNodeParser();
    private NodeList nextParsedNode = new NodeList();
    private boolean dontReadNextLine = false;
    /**
     * The constructor takes in a reader object, it's length and the url to be read.
     */
    public NodeReader(Reader in, int len, String url)
    {
        super(in, len);
        this.url = url;
        this.parser = null;
        this.lineCount = 1;
    }
    /**
     * This constructor basically overrides the existing constructor in the
     * BufferedReader class.
     * The URL defaults to an empty string.
     * @see #NodeReader(Reader,int,String)
     */

    public NodeReader(Reader in, int len)
    {
        this(in, len, "");
    }
    /**
     * The constructor takes in a reader object, and the url to be read.
     * The buffer size defaults to 8192.
     * @see #NodeReader(Reader,int,String)
     */
    public NodeReader(Reader in, String url)
    {
        this(in, 8192, url);
    }

    /**
     * Get the url for this reader.
     * @return The url specified in the constructor;
     */
    public String getURL()
    {
        return (url);
    }

    /**
     * This method is intended to be called only by scanners, when a situation of dirty html has arisen, 
     * and action has been taken to correct the parsed tags. For e.g. if we have html of the form :
     * <pre>
     * <a href="somelink.html"><img src=...><td><tr><a href="someotherlink.html">...</a>
     * </pre>
     * Now to salvage the first link, we'd probably like to insert an end tag somewhere (typically before the
     * second begin link tag). So that the parsing continues uninterrupted, we will need to change the existing
     * line being parsed, to contain the end tag in it. 
     */
    public void changeLine(String line)
    {
        this.line = line;
    }
    public String getCurrentLine()
    {
        return line;
    }
    /**
     * Get the last line number that the reader has read
     * @return int last line number read by the reader
     */
    public int getLastLineNumber()
    {
        return lineCount - 1;
    }

    /**
     * This method is useful when designing your own scanners. You might need to find out what is the location where the
     * reader has stopped last.
     * @return int Last position read by the reader
     */
    public int getLastReadPosition()
    {
        if (node != null)
            return node.elementEnd();
        else
            return 0;
    }

    /*
     * Read the next line
     * @return String containing the line
     */
    public String getNextLine()
    {
        try
        {
            previousLine = line;
            line = readLine();
            if (line != null)
                lineCount++;
            posInLine = 0;
            return line;
        }
        catch (IOException e)
        {
            System.err.println("I/O Exception occurred while reading!");
        }
        return null;
    }
    /**
     * Returns the parser object for which this reader exists
     * @return org.htmlparser.Parser
     */
    public Parser getParser()
    {
        return parser;
    }
    /**
     * Gets the previousOpenScanner.
     * @return Returns a TagScanner
     */
    public TagScanner getPreviousOpenScanner()
    {
        return previousOpenScanner;
    }

    /**
     * Returns true if the text at <code>pos</code> in <code>line</code> should be scanned as a tag.
     * Basically an open angle followed by a known special character or a letter.
     * @param line The current line being parsed.
     * @param pos The position in the line to examine.
     * @return <code>true</code> if we think this is the start of a tag.
     */
    private boolean beginTag(String line, int pos)
    {
        char ch;
        boolean ret;

        ret = false;

        if (pos + 2 <= line.length())
            if ('<' == line.charAt(pos))
            {
                ch = line.charAt(pos + 1);
                // the order of these tests might be optimized for speed
                if ('/' == ch
                    || '%' == ch
                    || Character.isLetter(ch)
                    || '!' == ch)
                    ret = true;
            }

        return (ret);
    }

    /**
     * Read the next element
     * @return Node - The next node
    	 */
    public Node readElement() throws ParserException
    {
        return (readElement(false));
    }

    /**
     * Read the next element
     * @param balance_quotes If <code>true</code> string nodes are parsed
     * paying attention to single and double quotes, such that tag-like
     * strings are ignored if they are quoted.
     * @return Node - The next node
    	 */
    public Node readElement(boolean balance_quotes) throws ParserException
    {
        try
        {
            if (nextParsedNode.size() > 0)
            {
                node = nextParsedNode.elementAt(0);
                nextParsedNode.remove(0);
                return node;
            }
            if (readNextLine())
            {
                do
                {
                    line = getNextLine();
                }
                while (line != null && line.length() == 0);

            }
            else if (dontReadNextLine)
            {
                dontReadNextLine = false;
            }
            else
                posInLine = getLastReadPosition() + 1;
            if (line == null)
                return null;

            if (beginTag(line, posInLine))
            {
                node = remarkNodeParser.find(this, line, posInLine);
                if (node != null)
                    return node;
                node = Tag.find(this, line, posInLine);
                if (node != null)
                {
                    Tag tag = (Tag) node;
                    try
                    {
                        node = tag.scan(parser.getScanners(), url, this);
                        return node;
                    }
                    catch (Exception e)
                    {
                        StringBuffer msgBuffer = new StringBuffer();
                        msgBuffer.append(
                            DECIPHER_ERROR
                                + "\n"
                                + "    Tag being processed : "
                                + tag.getTagName()
                                + "\n"
                                + "    Current Tag Line : "
                                + tag.getTagLine());
                        appendLineDetails(msgBuffer);
                        ParserException ex =
                            new ParserException(msgBuffer.toString(), e);

                        parser.getFeedback().error(msgBuffer.toString(), ex);
                        throw ex;
                    }
                }

                node = EndTag.find(line, posInLine);
                if (node != null)
                    return node;
            }
            else
            {
                node = stringParser.find(this, line, posInLine, balance_quotes);
                if (node != null)
                    return node;
            }

            return null;
        }
        catch (ParserException pe)
        {
            throw pe;
        }
        catch (Exception e)
        {
            StringBuffer msgBuffer =
                new StringBuffer("NodeReader.readElement() : Error occurred while trying to read the next element,");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            appendLineDetails(msgBuffer);
            msgBuffer.append("\n Caused by:\n").append(
                sw.getBuffer().toString());
            ParserException ex = new ParserException(msgBuffer.toString(), e);
            parser.getFeedback().error(msgBuffer.toString(), ex);
            throw ex;
        }
    }
    public void appendLineDetails(StringBuffer msgBuffer)
    {
        msgBuffer.append("\nat Line ");
        msgBuffer.append(getLineCount());
        msgBuffer.append(" : ");
        msgBuffer.append(getLine());
        msgBuffer.append("\nPrevious Line ").append(getLineCount() - 1);
        msgBuffer.append(" : ").append(getPreviousLine());
    }
    /**
     * Do we need to read the next line ?
     * @return true - yes/ false - no
     */
    protected boolean readNextLine()
    {
        if (dontReadNextLine)
        {
            return false;
        }
        if (posInLine == -1
            || (line != null && node.elementEnd() + 1 >= line.length()))
            return true;
        else
            return false;
    }
    /**
     * The setParser method is used by the parser to put its own object into the reader. This happens internally,
     * so this method is not generally for use by the developer or the user.
     */
    public void setParser(Parser newParser)
    {
        parser = newParser;
    }
    /**
     * Sets the previousOpenScanner.
     * @param previousOpenScanner The previousOpenScanner to set
     */
    public void setPreviousOpenScanner(TagScanner previousOpenScanner)
    {
        this.previousOpenScanner = previousOpenScanner;
    }

    /**
     * @param lineSeparator New Line separator to be used
     */
    public static void setLineSeparator(String lineSeparator)
    {
        Node.setLineSeparator(lineSeparator);
    }

    /**
     * Gets the line seperator that is being used
     * @return String
     */
    public static String getLineSeparator()
    {
        return (Node.getLineSeparator());
    }
    /**
     * Returns the lineCount.
     * @return int
     */
    public int getLineCount()
    {
        return lineCount;
    }

    /**
     * Returns the previousLine.
     * @return String
     */
    public String getPreviousLine()
    {
        return previousLine;
    }

    /**
     * Returns the line.
     * @return String
     */
    public String getLine()
    {
        return line;
    }

    /**
     * Sets the lineCount.
     * @param lineCount The lineCount to set
     */
    public void setLineCount(int lineCount)
    {
        this.lineCount = lineCount;
    }

    /**
     * Sets the posInLine.
     * @param posInLine The posInLine to set
     */
    public void setPosInLine(int posInLine)
    {
        this.posInLine = posInLine;
    }

    public void reset() throws IOException
    {
        super.reset();
        lineCount = 1;
        posInLine = -1;
    }

    public StringParser getStringParser()
    {
        return stringParser;
    }

    /**
     * Adds the given node on the front of an internal list of pre-parsed nodes.
     * Used in recursive calls where downstream nodes have been recognized in
     * order to parse the current node.
     * @param nextParsedNode The node that will be returned next by the reader.
     */
    public void addNextParsedNode(Node nextParsedNode)
    {
        this.nextParsedNode.prepend(nextParsedNode);
    }

    public boolean isDontReadNextLine()
    {
        return dontReadNextLine;
    }

    public void setDontReadNextLine(boolean dontReadNextLine)
    {
        this.dontReadNextLine = dontReadNextLine;
    }

}
