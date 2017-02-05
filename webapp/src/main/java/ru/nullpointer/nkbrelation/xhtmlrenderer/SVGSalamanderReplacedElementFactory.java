/*
 * {{{ header & license
 * Copyright (c) 2006 Patrick Wright
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package ru.nullpointer.nkbrelation.xhtmlrenderer;

import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGPanel;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import javax.swing.*;

import java.io.InputStream;
import java.net.URI;
import java.awt.*;


/**
 * Factory to create ReplacedElements for SVG embedded in our XML file, using
 * the Salamander library. Salamander in this case will return a Swing JPanel.
 *
 * modification: ankostyuk
 */
public class SVGSalamanderReplacedElementFactory implements ReplacedElementFactory {

    private static Logger logger = LoggerFactory.getLogger(SVGSalamanderReplacedElementFactory.class);
    //

    public ReplacedElement createReplacedElement(
            LayoutContext c,
            BlockBox box,
            UserAgentCallback uac,
            int cssWidth,
            int cssHeight) {

        SVGPanel panel = new SVGPanel();
        String content = null;
        JComponent cc = null;

        try {
            Element elem = box.getElement();
            if (elem == null || ! isSVGEmbedded(elem)) {
                return null;
            }

            // HACK: the easiest way to integrate with Salamander is to have it read
            // our SVG from a file--so, push the content to a temporary file, yuck!
            content = getSVGElementContent(elem);

//            String svgStr = elem.getTextContent();
            String svgHTML = getCharacterDataFromElement(elem);

            panel.setAntiAlias(true);
            panel.setBackground(Color.WHITE);
            panel.setOpaque(false);

            InputStream is = IOUtils.toInputStream(svgHTML, "UTF-8");
            SVGUniverse svgUniverse = new SVGUniverse();
            URI svgURI = svgUniverse.loadSVG(is, "/svg", true);

            panel.setSvgUniverse(svgUniverse);
            panel.setSvgURI(svgURI);

            int width = panel.getSVGWidth();
            int height = panel.getSVGHeight();

            if ( cssWidth > 0 ) {
                width = cssWidth;
            }

            if ( cssHeight > 0 ) {
                height = cssHeight;
            }

            String val = elem.getAttribute("width");
            if ( val != null && val.length() > 0 ) {
                width = Integer.valueOf(val).intValue();
            }
            val = elem.getAttribute("height");
            if ( val != null && val.length() > 0 ) {
                height = Integer.valueOf(val).intValue();
            }
            panel.setScaleToFit(true);
            panel.setPreferredSize(new Dimension(width, height));
            panel.setSize(panel.getPreferredSize());

            cc = panel;

            // is.close(); ?
        } catch (Exception e) {
            logger.error("Failed create SVG", e);
            cc = getDefaultJComponent(content, cssWidth, cssHeight);
        }
        if (cc == null) {
            return null;
        } else {
            SwingReplacedElement result = new SwingReplacedElement(cc);
            if (c.isInteractive()) {
                c.getCanvas().add(cc);
            }
            return result;
        }
    }

    private String getSVGElementContent(Element elem) {
        if (elem.getChildNodes().getLength() > 0) {
            return elem.getFirstChild().getNodeValue();
        } else {
            return "SVG";
        }
    }

    public String getCharacterDataFromElement(Element elem) {
        Node child = elem.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private boolean isSVGEmbedded(Element elem) {
        return elem.getNodeName().equals("object") && elem.getAttribute("type").equals("image/svg+xml");
    }

    private JComponent getDefaultJComponent(String content, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel comp = new JLabel(content);
        panel.add(comp, BorderLayout.CENTER);
        panel.setOpaque(false);
        if ( width > 0 && height > 0 ) {
            panel.setPreferredSize(new Dimension(width, height));
            panel.setSize(panel.getPreferredSize());
        } else {
            panel.setPreferredSize(comp.getPreferredSize());
            panel.setSize(comp.getPreferredSize());
        }
        return panel;
    }

    public void reset() {

    }

    public void remove(Element e) {

    }

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        // nothing to do ?
    }
}
