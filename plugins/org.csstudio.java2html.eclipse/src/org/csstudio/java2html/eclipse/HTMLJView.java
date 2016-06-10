package org.csstudio.java2html.eclipse;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Document;

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

public class HTMLJView {

    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());

    private FXCanvas p;
    private WebView v;


            
    @Inject
    public HTMLJView(Composite parent) {
        p = new FXCanvas(parent, SWT.NONE);
    }
    
    public Control getControl() {
            return p;
    }

    public void loadFX(URL pageUrl, final Class<?> clazz, final String m) {
        initFX();
        FXBrowsers.load(v, pageUrl, new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = clazz.getMethod(m);
                    @SuppressWarnings("unused")
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
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp, Color.ALICEBLUE);

        if (isDebugging())
            v.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
                    enableFirebug(v.getEngine());
                }
            });
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
     * 
     * @param engine
     *            the webEngine for which debugging is to be enabled. TODO:
     *            shouldn't get firebug from network
     */
    private static void enableFirebug(final WebEngine engine) {
        engine.executeScript(
                "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
    }
}
