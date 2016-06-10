package org.csstudio.java2html.eclipse;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.osgi.framework.Bundle;

public class HTMLJEditorPart {

    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());
    private static final String BUNDLE_NAME = "org.csstudio.java2html.scan";
    private static final String CLASS_NAME = "org.csstudio.java2html.scan.ChartModel";
    private static final String METHOD = "onPageLoad";

    private static final String HTMLJ_CONTEXT_ID = "org.csstudio.java2html.eclipse.context";

    @Inject
    private Composite parent;

    @Inject
    private MUILabel partLabel;

    @Inject
    private IEditorInput input;

    @Inject
    private IEditorSite site;

    @Inject
    private IContextService contextService;

    private HTMLJView viewer;

    @PostConstruct
    public void initialize() {
        final FillLayout layout = new FillLayout();
        parent.setLayout(layout);
        refresh();
    }

    @PersistState
    public void persist() {
    }

    @Focus
    public void setFocus() {
        long time = System.currentTimeMillis();
        if (viewer != null) {
            viewer.getControl().forceFocus();
        }
    }

    public void refresh() {
        if (input == null) {
            LOG.log(Level.WARNING, "Input null");
            return;
        }
        long time = System.currentTimeMillis();
        partLabel.setLabel(input.getName());

        if (viewer == null) {
            viewer = new HTMLJView(parent);
            viewer.getControl().addFocusListener(new FocusListener() {
                private IContextActivation activation;

                @Override
                public void focusLost(FocusEvent e) {
                    if (activation != null) {
                        contextService.deactivateContext(activation);
                        activation = null;
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (activation == null) {
                        activation = contextService.activateContext(HTMLJ_CONTEXT_ID);
                    }
                }
            });

        }
        URI indexHtml = null;
        // need to pass input as refreshable
        Object adapter = input.getAdapter(IFile.class);
        if (adapter instanceof IFile) {
            IFile ifile = ((IFile) adapter);
            indexHtml = ifile.getLocationURI();
        }
        if (indexHtml == null) {
            LOG.log(Level.WARNING, "Can't load input");
            return;
        }

        Bundle b = org.eclipse.core.runtime.Platform.getBundle(BUNDLE_NAME);
        try {
            viewer.loadFX(indexHtml.toURL(), b.loadClass(CLASS_NAME), METHOD);

        } catch (ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }

    }

}
