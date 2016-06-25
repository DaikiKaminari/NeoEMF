/*
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.io.input.xmi;

import fr.inria.atlanmod.neoemf.io.InternalHandler;
import fr.inria.atlanmod.neoemf.logger.NeoLogger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;

import javax.xml.parsers.SAXParserFactory;

/**
 *
 */
public class XmiSaxReader extends AbstractXmiReader {

    @Override
    public InternalHandler newHandler() {
        return new XmiHandler();
    }

    public void read(File file) throws Exception {
        if (!hasHandler()) {
            NeoLogger.error("This notifier hasn't any handler.");
            return;
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.newSAXParser().parse(file, new XmiSaxHandler());
    }

    private class XmiSaxHandler extends DefaultHandler {

        @Override
        public void startDocument() throws SAXException {
            try {
                XmiSaxReader.this.notifyStartDocument();
            }
            catch (Exception e) {
                NeoLogger.error(e);
                throw new SAXException(e);
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            processNamespace(prefix, uri);
        }

        @Override
        public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException
        {
            try {
                processElement(uri, name, attributes);
            }
            catch (Exception e) {
                NeoLogger.error(e);
                throw new SAXException(e);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String characters = String.valueOf(ch, start, length);

            if (!characters.trim().isEmpty()) {
                // TODO Processes CDATA
            }
        }

        @Override
        public void endElement(String uri, String name, String qName) throws SAXException {
            try {
                XmiSaxReader.this.notifyEndElement();
            }
            catch (Exception e) {
                NeoLogger.error(e);
                throw new SAXException(e);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            try {
                XmiSaxReader.this.notifyEndDocument();
            }
            catch (Exception e) {
                NeoLogger.error(e);
                throw new SAXException(e);
            }
        }
    }
}
