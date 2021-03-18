/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.sax;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Basic factory for creating common types of ContentHandlers
 */
public class BasicContentHandlerFactory implements ContentHandlerFactory {

    private final HANDLER_TYPE type;
    private final int writeLimit;

    /**
     * @param type       basic type of handler
     * @param writeLimit max number of characters to store; if < 0,
     *                   the handler will store all characters
     */
    public BasicContentHandlerFactory(HANDLER_TYPE type, int writeLimit) {
        this.type = type;
        this.writeLimit = writeLimit;
    }

    /**
     * Tries to parse string into handler type.  Returns default if string is null or
     * parse fails.
     * <p/>
     * Options: xml, html, text, body, ignore (no content)
     *
     * @param handlerTypeName string to parse
     * @param defaultType     type to return if parse fails
     * @return handler type
     */
    public static HANDLER_TYPE parseHandlerType(String handlerTypeName, HANDLER_TYPE defaultType) {
        if (handlerTypeName == null) {
            return defaultType;
        }

        String lcHandlerTypeName = handlerTypeName.toLowerCase(Locale.ROOT);
        switch (lcHandlerTypeName) {
            case "xml":
                return HANDLER_TYPE.XML;
            case "text":
                return HANDLER_TYPE.TEXT;
            case "txt":
                return HANDLER_TYPE.TEXT;
            case "html":
                return HANDLER_TYPE.HTML;
            case "body":
                return HANDLER_TYPE.BODY;
            case "ignore":
                return HANDLER_TYPE.IGNORE;
            default:
                return defaultType;
        }
    }

    @Override
    public ContentHandler getNewContentHandler() {

        if (type == HANDLER_TYPE.BODY) {
            return new BodyContentHandler(writeLimit);
        } else if (type == HANDLER_TYPE.IGNORE) {
            return new DefaultHandler();
        }
        if (writeLimit > -1) {
            switch (type) {
                case TEXT:
                    return new WriteOutContentHandler(new ToTextContentHandler(), writeLimit);
                case HTML:
                    return new WriteOutContentHandler(new ToHTMLContentHandler(), writeLimit);
                case XML:
                    return new WriteOutContentHandler(new ToXMLContentHandler(), writeLimit);
                default:
                    return new WriteOutContentHandler(new ToTextContentHandler(), writeLimit);
            }
        } else {
            switch (type) {
                case TEXT:
                    return new ToTextContentHandler();
                case HTML:
                    return new ToHTMLContentHandler();
                case XML:
                    return new ToXMLContentHandler();
                default:
                    return new ToTextContentHandler();

            }
        }
    }

    @Override
    public ContentHandler getNewContentHandler(OutputStream os, String encoding)
            throws UnsupportedEncodingException {
        return getNewContentHandler(os, Charset.forName(encoding));
    }

    @Override
    public ContentHandler getNewContentHandler(OutputStream os, Charset charset) {

        if (type == HANDLER_TYPE.IGNORE) {
            return new DefaultHandler();
        }
        try {
            if (writeLimit > -1) {
                switch (type) {
                    case BODY:
                        return new WriteOutContentHandler(
                                new BodyContentHandler(new OutputStreamWriter(os, charset)),
                                writeLimit);
                    case TEXT:
                        return new WriteOutContentHandler(
                                new ToTextContentHandler(os, charset.name()), writeLimit);
                    case HTML:
                        return new WriteOutContentHandler(
                                new ToHTMLContentHandler(os, charset.name()), writeLimit);
                    case XML:
                        return new WriteOutContentHandler(
                                new ToXMLContentHandler(os, charset.name()), writeLimit);
                    default:
                        return new WriteOutContentHandler(
                                new ToTextContentHandler(os, charset.name()), writeLimit);
                }
            } else {
                switch (type) {
                    case BODY:
                        return new BodyContentHandler(new OutputStreamWriter(os, charset));
                    case TEXT:
                        return new ToTextContentHandler(os, charset.name());
                    case HTML:
                        return new ToHTMLContentHandler(os, charset.name());
                    case XML:
                        return new ToXMLContentHandler(os, charset.name());
                    default:
                        return new ToTextContentHandler(os, charset.name());

                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("couldn't find charset for name: " + charset);
        }
    }

    /**
     * @return handler type used by this factory
     */
    public HANDLER_TYPE getType() {
        return type;
    }

    /**
     * Common handler types for content.
     */
    public enum HANDLER_TYPE {
        BODY, IGNORE, //don't store content
        TEXT, HTML, XML
    }

    public int getWriteLimit() {
        return writeLimit;
    }
}
