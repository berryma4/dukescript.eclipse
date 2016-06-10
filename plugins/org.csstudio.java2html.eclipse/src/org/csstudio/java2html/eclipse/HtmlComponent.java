package org.csstudio.java2html.eclipse;

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.java.html.boot.fx.FXBrowsers;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

public class HtmlComponent extends ViewPart implements IExecutableExtension {
    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());

    private FXCanvas p;
    private WebView v;

    private String bundleName = "";
    private String indexHtml = "";
    private String className = "";
    private String method = "onPageLoad";

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        p = new FXCanvas(parent, SWT.NONE);
        Bundle b = org.eclipse.core.runtime.Platform.getBundle(bundleName);
        try {
            loadFX(b.getResource(indexHtml), b.loadClass(className), method);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFX(URL pageUrl, final Class<?> clazz, final String m) {
        initFX();
        FXBrowsers.load(v, pageUrl, new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = clazz.getMethod(m);
                    Object value = method.invoke(null);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Can't load " + m + " from " + clazz, ex);
                }
            }
        });
    }

    private void initFX() {
        Platform.setImplicitExit(false);
        v = new WebView();
        v.getEngine().setJavaScriptEnabled(true);
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp, Color.ALICEBLUE);

        class X implements ChangeListener<String>, Runnable {

            private String title;

            public X() {
                super();
            }

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                title = v.getEngine().getTitle();
                EventQueue.invokeLater(this);
            }

            @Override
            public void run() {
                if (title != null) {
                	System.err.println("UPDATING TITLE");
                }
            }
        }
        final X x = new X();
        v.getEngine().titleProperty().addListener(x);
        Platform.runLater(x);
        if (isDebugging()){
            v.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
                    @Override
                    public void changed(ObservableValue<? extends Document> prop, 
                                        Document oldDoc, Document newDoc) {
                            enableFirebug(v.getEngine());
                    }
            });
        }
        bp.setCenter(v);
        p.setScene(scene);
    }

    private static boolean isDebugging() {
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
                .indexOf("jdwp") >= 0;
        return isDebug;
    }

    /**
     * Enables Firebug Lite for debugging a webEngine.
     * @param engine the webEngine for which debugging is to be enabled.
     * TODO: shouldn't get firebug from network
     */
    private static void enableFirebug(final WebEngine engine) {
            engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        for (IConfigurationElement c : cfig.getChildren()) {
            if ("parameter".equals(c.getName())) {
                switch (c.getAttribute("name")) {
                case "bundle":
                    this.bundleName = c.getAttribute("value");
                    break;
                case "html":
                    this.indexHtml = c.getAttribute("value");
                    break;
                case "className":
                    this.className = c.getAttribute("value");
                    break;
                case "mainMethod":
                    this.method = c.getAttribute("value");
                    break;
                default:
                    break;
                }
            }

        }
        super.setInitializationData(cfig, propertyName, data);
    }
}
