package org.csstudio.java2html.eclipse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swt.FXCanvas;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import net.java.html.boot.fx.FXBrowsers;

public class HTMLJE3Editor extends EditorPart {
    
    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());

    public static final String EDITOR_ID = "org.csstudio.java2html.eclipse.E3Editor";
    
    private FXCanvas canvas;

    private String bundleName = "";
    private String className = "";
    private String method = "onPageLoad";

    
    @Override
    public void doSave(IProgressMonitor monitor) {
        
    }

    @Override
    public void doSaveAs() {
        
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        Platform.setImplicitExit(false);
        canvas = new FXCanvas(parent, SWT.NONE);
        WebView view = new WebView();
        view.getEngine().setJavaScriptEnabled(true);
        
        

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("noborder-scroll-pane");
        Scene scene = new Scene(scrollPane);
        scrollPane.setContent(view);

        canvas.setScene(scene);

        if (isDebugging()){
            view.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
                    @Override
                    public void changed(ObservableValue<? extends Document> prop, 
                                        Document oldDoc, Document newDoc) {
                            enableFirebug(view.getEngine());
                    }
            });
        }
        Bundle b = org.eclipse.core.runtime.Platform.getBundle(bundleName);
        URI indexHtml = null;
        // need to pass input as refreshable
        Object adapter = getEditorInput().getAdapter(IFile.class);
        if (adapter instanceof IFile) {
            IFile ifile = ((IFile) adapter);
            indexHtml = ifile.getLocationURI();
        }
        if (indexHtml == null) {
            LOG.log(Level.WARNING, "Can't load input");
            return;
        }
        Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList("text/html"));
        try {
            java.net.CookieHandler.getDefault().put(indexHtml, headers);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            final Class<?> clazz  = b.loadClass(className);
            final String m = method;
            FXBrowsers.load(view, indexHtml.toURL(), new Runnable() {
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

        } catch (ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }

        setPartName(getEditorInput().getName());
        
//        HBox toolBar = new HBox();
//        toolBar.setAlignment(Pos.CENTER);
//        toolBar.getStyleClass().add("browser-toolbar");
//        WebView smallView = new WebView();
//        smallView.setPrefSize(120, 80);
//        engine.setCreatePopupHandler(
//                new Callback<PopupFeatures, WebEngine>() {
//                    @Override public WebEngine call(PopupFeatures config) {
//                        smallView.setFontScale(0.8);
//                        if (!toolBar.getChildren().contains(smallView)) {
//                            toolBar.getChildren().add(smallView);
//                        }
//                        return smallView.getEngine();
//                    }
//                 }
//            );
//        
        
    }

    @Override
    public void setFocus() {
        if (canvas != null) {
            canvas.setFocus();
    }
        
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
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        for (IConfigurationElement c : cfig.getChildren()) {
            if ("parameter".equals(c.getName())) {
                switch (c.getAttribute("name")) {
                case "bundle":
                    this.bundleName = c.getAttribute("value");
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